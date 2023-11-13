package org.jisho.textosJapones.database.dao.implement;

import org.jisho.textosJapones.database.dao.NovelDao;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.entities.novelextractor.*;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class NovelDaoJDBC implements NovelDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(NovelDaoJDBC.class);

    private final Connection conn;
    private final String schema;

    final private String CREATE_TABELA = "CALL create_table('%s');";
    final private String TABELA_VOLUME = "_volumes";
    final private String TABELA_CAPITULO = "_capitulos";
    final private String TABELA_TEXTO = "_textos";
    final private String TABELA_VOCABULARIO = "_vocabularios";

    final private String CREATE_TRIGGER_INSERT =
            "CREATE TRIGGER tr_%s_insert BEFORE INSERT ON %s" +
                    "  FOR EACH ROW BEGIN" +
                    "    IF (NEW.id IS NULL OR NEW.id = '') THEN" +
                    "      SET new.id = UUID();" +
                    "    END IF;" +
                    "  END";

    final private String CREATE_TRIGGER_UPDATE =
            "CREATE TRIGGER tr_%s_update BEFORE UPDATE ON %s" +
                    "  FOR EACH ROW BEGIN" +
                    "    SET new.Atualizacao = NOW();" +
                    "  END";


    final private String INSERT_VOLUMES = "INSERT INTO %s_volumes (id, novel, titulo, titulo_alternativo, descricao, editora, volume, linguagem, arquivo, is_processado) VALUES (?,?,?,?,?,?,?,?,?,?)";
    final private String INSERT_CAPITULOS = "INSERT INTO %s_capitulos (id, id_volume, novel, volume, capitulo, linguagem, is_processado) VALUES (?,?,?,?,?,?,?)";
    final private String INSERT_TEXTO = "INSERT INTO %s_textos (id, id_capitulo, sequencia, texto) VALUES (?,?,?,?)";
    final private String INSERT_CAPA = "INSERT INTO %s_capas (id, id_volume, novel, volume, linguagem, capa) VALUES (?,?,?,?,?,?)";

    final private String DELETE_VOLUMES = "DELETE v FROM %s_volumes AS v %s";
    final private String DELETE_CAPITULOS = "DELETE c FROM %s_capitulos AS c INNER JOIN %s_volumes AS v ON v.id = c.id_volume %s";
    final private String DELETE_TEXTOS = "DELETE t FROM %s_textos AS t INNER JOIN %s_capitulos AS c ON c.id = t.id_capitulo INNER JOIN %s_volumes AS v ON v.id = c.id_volume %s";

    final private String SELECT_VOLUMES = "SELECT VOL.id, VOL.novel, VOL.titulo, VOL.titulo_alternativo, VOL.titulo_alternativo, VOL.descricao, VOL.editora, VOL.volume, VOL.linguagem, VOL.arquivo, VOL.is_Processado FROM %s_volumes VOL WHERE %s GROUP BY VOL.id ORDER BY VOL.novel, VOL.linguagem, VOL.volume";
    final private String SELECT_CAPITULOS = "SELECT CAP.id, CAP.novel, CAP.volume, CAP.capitulo, CAP.linguagem, CAP.is_processado "
            + "FROM %s_capitulos CAP %s WHERE id_volume = ? AND %s GROUP BY CAP.id ORDER BY CAP.linguagem, CAP.volume";
    final private String SELECT_TEXTOS = "SELECT id, sequencia, texto FROM %s_textos WHERE id_capitulo = ? ";

    final private String SELECT_CAPA = "SELECT id, novel, volume, linguagem, capa FROM %s_capas WHERE id_volume = ? ";

    final private String FIND_VOLUME = "SELECT VOL.id, VOL.novel, VOL.titulo, VOL.titulo_alternativo, VOL.titulo_alternativo, VOL.descricao, VOL.editora, VOL.volume, VOL.linguagem, VOL.arquivo, VOL.is_Processado FROM %s_volumes VOL WHERE novel = ? AND volume = ? AND linguagem = ? LIMIT 1";
    final private String SELECT_VOLUME = "SELECT VOL.id, VOL.novel, VOL.titulo, VOL.titulo_alternativo, VOL.titulo_alternativo, VOL.descricao, VOL.editora, VOL.volume, VOL.linguagem, VOL.arquivo, VOL.is_Processado FROM %s_volumes VOL WHERE id = ?";
    final private String SELECT_CAPITULO = "SELECT CAP.id, CAP.novel, CAP.volume, CAP.capitulo, CAP.linguagem, CAP.is_processado FROM %s_capitulos CAP WHERE id = ?";

    final private String SELECT_TABELAS = "SELECT REPLACE(Table_Name, '_volumes', '') AS Tabela "
            + "FROM information_schema.tables WHERE table_schema = '%s' AND Table_Name NOT LIKE '%%exemplo%%' "
            + "AND Table_Name LIKE '%%_volumes' AND %s GROUP BY Tabela ";

    final private String SELECT_LISTA_TABELAS = "SELECT REPLACE(Table_Name, '_volumes', '') AS Tabela "
            + " FROM information_schema.tables WHERE table_schema = '%s' AND %s "
            + " AND Table_Name LIKE '%%_volumes%%' GROUP BY Tabela ";
    final private String DELETE_VOCABULARIO = "DELETE FROM %s_vocabularios WHERE %s = ?;";
    final private String INSERT_VOCABULARIO = "INSERT INTO %s_vocabularios (%s, palavra, portugues, ingles, leitura, revisado) "
            + " VALUES (?,?,?,?,?,?);";
    final private String SELECT_VOCABUALARIO = "SELECT id, palavra, portugues, ingles, leitura, revisado FROM %s_vocabularios WHERE %s ";

    public NovelDaoJDBC(Connection conn, String base) {
        this.conn = conn;
        this.schema = base;
    }

    private List<Language> getLinguagem(Language... linguagem) {
        List<Language> list = new ArrayList<>();
        for (Language lang : linguagem)
            if (lang != null)
                list.add(lang);

        return list;
    }

    @Override
    public void insertVocabulario(String base, UUID idVolume, UUID idCapitulo, Set<NovelVocabulario> vocabulario) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            if (idVolume == null && idCapitulo == null)
                return;

            String campo = idVolume != null ? "id_volume" : "id_capitulo";
            UUID id = idVolume != null ? idVolume : idCapitulo;
            clearVocabulario(base, campo, id);

            for (NovelVocabulario vocab : vocabulario) {
                st = conn.prepareStatement(String.format(INSERT_VOCABULARIO, base, campo), Statement.RETURN_GENERATED_KEYS);

                st.setString(1, id.toString());
                st.setString(2, vocab.getPalavra());
                st.setString(3, vocab.getPortugues());
                st.setString(4, vocab.getIngles());
                st.setString(5, vocab.getLeitura());
                st.setBoolean(6, vocab.getRevisado());

                st.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
        } finally {
            DB.closeStatement(st);
        }
    }

    private void clearVocabulario(String base, String campo, UUID id) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(String.format(DELETE_VOCABULARIO, base, campo));
            st.setString(1, id.toString());
            st.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
        } finally {
            DB.closeStatement(st);
        }
    }

    private Set<NovelVocabulario> selectVocabulario(String base, String where) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(String.format(SELECT_VOCABUALARIO, base, where));
            rs = st.executeQuery();

            Set<NovelVocabulario> list = new HashSet<>();

            while (rs.next()) {
                // Ignora os kanji solto.
                if (rs.getString("palavra").length() <= 1)
                    continue;
                list.add(new NovelVocabulario(UUID.fromString(rs.getString("id")), rs.getString("palavra"), rs.getString("portugues"),
                        rs.getString("ingles"), rs.getString("leitura"), rs.getBoolean("revisado")));
            }
            return list;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    public List<NovelVolume> selectVolumes(String base, String novel, Integer volume, List<Language> linguagem) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            String condicao = " 1>0 ";

            if (linguagem != null && !linguagem.isEmpty()) {
                String lang = "";
                for (Language lg : linguagem)
                    lang += " VOL.linguagem = '" + lg.getSigla() + "' OR ";

                condicao += " AND (" + lang.substring(0, lang.lastIndexOf(" OR ")) + ")";
            }

            if (novel != null && !novel.trim().isEmpty())
                condicao += " AND VOL.novel LIKE " + '"' + novel.trim() + '"';

            if (volume != null && volume > 0)
                condicao += " AND VOL.volume = " + volume;

            st = conn.prepareStatement(String.format(SELECT_VOLUMES, base, condicao));
            rs = st.executeQuery();

            List<NovelVolume> list = new ArrayList<>();

            while (rs.next())
                list.add(new NovelVolume(UUID.fromString(rs.getString("id")), rs.getString("novel"),
                        rs.getString("titulo"), rs.getString("titulo_alternativo"), rs.getString("descricao"),
                        rs.getString("arquivo"), rs.getString("editora"), rs.getInt("volume"), Language.getEnum(rs.getString("linguagem")),
                        selectCapa(base, UUID.fromString(rs.getString("id"))), rs.getBoolean("is_processado"),
                        selectCapitulos(base, UUID.fromString(rs.getString("id")), linguagem),
                        selectVocabulario(base, "id_volume = " + '"' + UUID.fromString(rs.getString("id")) + '"')));
            return list;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    public List<NovelCapitulo> selectCapitulos(String base, UUID idVolume, List<Language> linguagem) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            String inner = "";
            String condicao = " 1>0 ";

            if (linguagem != null && !linguagem.isEmpty()) {
                String lang = "";
                for (Language lg : linguagem)
                    lang += " CAP.linguagem = '" + lg.getSigla() + "' OR ";

                condicao += " AND (" + lang.substring(0, lang.lastIndexOf(" OR ")) + ")";
            }

            st = conn.prepareStatement(String.format(SELECT_CAPITULOS, base, inner, condicao));
            st.setString(1, idVolume.toString());
            rs = st.executeQuery();

            List<NovelCapitulo> list = new ArrayList<>();

            while (rs.next())
                list.add(new NovelCapitulo(UUID.fromString(rs.getString("id")), rs.getString("novel"), rs.getInt("volume"),
                        rs.getFloat("capitulo"), rs.getInt("sequencia"), Language.getEnum(rs.getString("linguagem")),
                        rs.getBoolean("is_processado"), selectTextos(base, UUID.fromString(rs.getString("id"))),
                        selectVocabulario(base, "id_capitulo = " + '"' + UUID.fromString(rs.getString("id")) + '"')));

            return list;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    public List<NovelTexto> selectTextos(String base, UUID idCapitulo) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(String.format(SELECT_TEXTOS, base));
            st.setString(1, idCapitulo.toString());
            rs = st.executeQuery();

            List<NovelTexto> list = new ArrayList<>();

            while (rs.next())
                list.add(new NovelTexto(UUID.fromString(rs.getString("id")), rs.getString("texto"), rs.getInt("sequencia")));

            return list;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    public NovelCapa selectCapa(String base, UUID idNovel) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(String.format(SELECT_CAPA, base));
            st.setString(1, idNovel.toString());
            rs = st.executeQuery();

            if (rs.next())
                return new NovelCapa(UUID.fromString(rs.getString("id")), rs.getString("novel"),
                        rs.getInt("volume"), Language.getEnum(rs.getString("linguagem")), null);
            else
                return null;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    @Override
    public NovelVolume selectVolume(String base, String novel, Integer volume, Language linguagem) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(String.format(FIND_VOLUME, base));
            st.setString(1, novel);
            st.setInt(2, volume);
            st.setString(3, linguagem.getSigla());
            rs = st.executeQuery();

            if (rs.next())
                return new NovelVolume(UUID.fromString(rs.getString("id")), rs.getString("novel"),
                        rs.getString("titulo"), rs.getString("titulo_alternativo"), rs.getString("descricao"),
                        rs.getString("arquivo"), rs.getString("editora"), rs.getInt("volume"), Language.getEnum(rs.getString("linguagem")),
                        selectCapa(base, UUID.fromString(rs.getString("id"))), rs.getBoolean("is_processado"),
                        selectCapitulos(base, UUID.fromString(rs.getString("id")), getLinguagem(linguagem)),
                        selectVocabulario(base, "id_volume = " + '"' + UUID.fromString(rs.getString("id")) + '"'));
            return null;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    @Override
    public NovelVolume selectVolume(String base, UUID id) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(String.format(SELECT_VOLUME, base));
            st.setString(1, id.toString());
            rs = st.executeQuery();

            if (rs.next())
                return new NovelVolume(UUID.fromString(rs.getString("id")), rs.getString("novel"),
                        rs.getString("titulo"), rs.getString("titulo_alternativo"), rs.getString("descricao"),
                        rs.getString("arquivo"), rs.getString("editora"), rs.getInt("volume"), Language.getEnum(rs.getString("linguagem")),
                        selectCapa(base, UUID.fromString(rs.getString("id"))), rs.getBoolean("is_processado"),
                        selectCapitulos(base, UUID.fromString(rs.getString("id")), null),
                        selectVocabulario(base, "id_volume = " + '"' + UUID.fromString(rs.getString("id")) + '"'));
            return null;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    @Override
    public NovelCapitulo selectCapitulo(String base, UUID id) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(String.format(SELECT_CAPITULO, base));
            st.setString(1, id.toString());
            rs = st.executeQuery();

            if (rs.next())
                return new NovelCapitulo(UUID.fromString(rs.getString("id")), rs.getString("novel"), rs.getInt("volume"),
                        rs.getFloat("capitulo"), rs.getInt("sequencia"), Language.getEnum(rs.getString("linguagem")),
                        rs.getBoolean("is_processado"), selectTextos(base, UUID.fromString(rs.getString("id"))),
                        selectVocabulario(base, "id_capitulo = " + '"' + UUID.fromString(rs.getString("id")) + '"'));

            return null;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    @Override
    public List<NovelTabela> selectAll(String base) throws ExcessaoBd {
        return selectAll(base, "", 0, 0F, null);
    }

    @Override
    public List<NovelTabela> selectAll(String base, String novel, Integer volume, Float capitulo, Language linguagem) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            String condicao = "1>0 ";

            if (base != null && !base.trim().isEmpty())
                condicao += " AND Table_Name LIKE '%" + base.trim() + "%'";

            st = conn.prepareStatement(
                    String.format(SELECT_TABELAS, schema, condicao));
            rs = st.executeQuery();

            List<NovelTabela> list = new ArrayList<>();

            while (rs.next()) {
                List<NovelVolume> volumes = selectVolumes(rs.getString("Tabela"), novel, volume, getLinguagem(linguagem));
                if (volumes.size() > 0)
                    list.add(new NovelTabela(rs.getString("Tabela"), volumes));
            }

            return list;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    @Override
    public void deleteVocabulario(String base) throws ExcessaoBd {
        PreparedStatement stVolume = null;
        PreparedStatement stCapitulo = null;
        PreparedStatement stVocabulario = null;
        try {
            stVocabulario = conn.prepareStatement(String.format("DELETE FROM %s_vocabulario", base));
            stCapitulo = conn.prepareStatement(String.format("UPDATE %s_capitulos SET is_processado = 0", base));
            stVolume = conn.prepareStatement(String.format("UPDATE %s_volumes SET is_processado = 0", base));

            conn.setAutoCommit(false);
            conn.beginRequest();
            stVocabulario.executeUpdate();
            stCapitulo.executeUpdate();
            stVolume.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            System.out.println(stVocabulario.toString());
            System.out.println(stCapitulo.toString());
            System.out.println(stVolume.toString());

            LOGGER.error(e.getMessage(), e);
            throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.error(e.getMessage(), e);
            }
            DB.closeStatement(stVocabulario);
            DB.closeStatement(stCapitulo);
            DB.closeStatement(stVolume);
        }
    }

    @Override
    public void deleteVolume(String base, NovelVolume obj) throws ExcessaoBd {
        PreparedStatement stVolume = null;
        PreparedStatement stCapitulo = null;
        PreparedStatement stTexto = null;
        try {
            String where = "WHERE ";
            if (obj.getId() != null)
                where += " v.id = " + obj.getId().toString();
            else
                where += " v.novel = '" + obj.getNovel() + "' AND v.volume = " + obj.getVolume().toString()
                        + " AND v.linguagem = '" + obj.getLingua().getSigla() + "'";

            String caminhoBase = base;

            stTexto = conn.prepareStatement(String.format(DELETE_TEXTOS, caminhoBase, caminhoBase, caminhoBase, caminhoBase, where));
            stCapitulo = conn.prepareStatement(String.format(DELETE_CAPITULOS, caminhoBase, caminhoBase, where));
            stVolume = conn.prepareStatement(String.format(DELETE_VOLUMES, caminhoBase, where));

            conn.setAutoCommit(false);
            conn.beginRequest();
            stTexto.executeUpdate();
            stCapitulo.executeUpdate();
            stVolume.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            System.out.println(stTexto.toString());
            System.out.println(stCapitulo.toString());
            System.out.println(stVolume.toString());

            LOGGER.error(e.getMessage(), e);
            throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.error(e.getMessage(), e);
            }
            DB.closeStatement(stTexto);
            DB.closeStatement(stCapitulo);
            DB.closeStatement(stVolume);
        }
    }

    @Override
    public void deleteCapitulo(String base, NovelCapitulo obj) throws ExcessaoBd {
        PreparedStatement stCapitulo = null;
        PreparedStatement stTexto = null;
        try {
            String where = "WHERE c.id = " + obj.getId().toString();
            String caminhoBase = base;

            stTexto = conn.prepareStatement(String.format(DELETE_TEXTOS, caminhoBase, caminhoBase, caminhoBase, caminhoBase, where));
            stCapitulo = conn.prepareStatement(String.format(DELETE_CAPITULOS, caminhoBase, caminhoBase, where));

            conn.setAutoCommit(false);
            conn.beginRequest();
            stTexto.executeUpdate();
            stCapitulo.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            System.out.println(stTexto.toString());
            System.out.println(stCapitulo.toString());

            LOGGER.error(e.getMessage(), e);
            throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {

                LOGGER.error(e.getMessage(), e);
            }
            DB.closeStatement(stTexto);
            DB.closeStatement(stCapitulo);
        }

    }

    @Override
    public void deleteTexto(String base, NovelTexto obj) throws ExcessaoBd {
        PreparedStatement stTexto = null;
        try {
            String where = "WHERE t.id = " + obj.getId().toString();
            String caminhoBase = base;

            stTexto = conn.prepareStatement(
                    String.format(DELETE_TEXTOS, caminhoBase, caminhoBase, caminhoBase, caminhoBase, where));
            conn.setAutoCommit(false);
            conn.beginRequest();
            stTexto.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            System.out.println(stTexto.toString());

            LOGGER.error(e.getMessage(), e);
            throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {

                LOGGER.error(e.getMessage(), e);
            }
            DB.closeStatement(stTexto);
        }

    }

    @Override
    public UUID insertVolume(String base, NovelVolume obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(String.format(INSERT_VOLUMES, base),
                    Statement.RETURN_GENERATED_KEYS);

            Integer index = 0;
            st.setString(++index, obj.getId().toString());
            st.setString(++index, obj.getNovel());
            st.setString(++index, obj.getTitulo());
            st.setString(++index, obj.getTituloAlternativo());
            st.setString(++index, obj.getDescricao());
            st.setString(++index, obj.getEditora());
            st.setInt(++index, obj.getVolume());
            st.setString(++index, obj.getLingua().getSigla());
            st.setString(++index, obj.getArquivo());
            st.setBoolean(++index, obj.getProcessado());

            int rowsAffected = st.executeUpdate();

            if (rowsAffected < 1) {
                LOGGER.info(st.toString());
                throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
            } else {
                if (obj.getCapa() != null)
                    obj.getCapa().setId(insertCapa(base, obj.getId(), obj.getCapa()));

                insertVocabulario(base, obj.getId(), null, obj.getVocabularios());
                return obj.getId();
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public UUID insertCapitulo(String base, UUID idVolume, NovelCapitulo obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(String.format(INSERT_CAPITULOS, base), Statement.RETURN_GENERATED_KEYS);

            Integer index = 0;
            st.setString(++index, obj.getId().toString());
            st.setString(++index, idVolume.toString());
            st.setString(++index, obj.getNovel());
            st.setInt(++index, obj.getVolume());
            st.setFloat(++index, obj.getCapitulo());
            st.setString(++index, obj.getLingua().getSigla());
            st.setBoolean(++index, obj.getProcessado());

            int rowsAffected = st.executeUpdate();

            if (rowsAffected < 1) {
                LOGGER.info(st.toString());
                throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
            } else {
                insertVocabulario(base, null, obj.getId(), obj.getVocabularios());
                return obj.getId();
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public UUID insertTexto(String base, UUID idCapitulo, NovelTexto obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(String.format(INSERT_TEXTO, base), Statement.RETURN_GENERATED_KEYS);

            Integer index = 0;
            st.setString(++index, obj.getId().toString());
            st.setString(++index, idCapitulo.toString());
            st.setInt(++index, obj.getSequencia());
            st.setString(++index, obj.getTexto());

            int rowsAffected = st.executeUpdate();

            if (rowsAffected < 1) {
                LOGGER.info(st.toString());
                throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
            } else
                return obj.getId();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
        } finally {
            DB.closeStatement(st);
        }
    }

    public UUID insertCapa(String base, UUID idVolume, NovelCapa obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(String.format(INSERT_CAPA, base), Statement.RETURN_GENERATED_KEYS);

            Integer index = 0;
            st.setString(++index, obj.getId().toString());
            st.setString(++index, idVolume.toString());
            st.setString(++index, obj.getNovel());
            st.setInt(++index, obj.getVolume());
            st.setString(++index, obj.getLingua().getSigla());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(obj.getImagem(), "jpg", baos);

            st.setBinaryStream(++index, new ByteArrayInputStream(baos.toByteArray()));

            int rowsAffected = st.executeUpdate();

            if (rowsAffected < 1) {
                LOGGER.info(st.toString());
                throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
            } else
                return obj.getId();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            DB.closeStatement(st);
        }
    }

    private void createTriggers(String nome) throws ExcessaoBd {
        PreparedStatement st = null;

        try {
            st = conn.prepareStatement(String.format(CREATE_TRIGGER_INSERT, nome, nome));
            st.execute();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
        } finally {
            DB.closeStatement(st);
        }

        try {
            st = conn.prepareStatement(String.format(CREATE_TRIGGER_UPDATE, nome, nome));
            st.execute();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public void createTabela(String baseDestino) throws ExcessaoBd {
        String nome = baseDestino.trim();
        if (nome.contains("."))
            nome = baseDestino.substring(baseDestino.indexOf(".")).replace(".", "");

        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(String.format(CREATE_TABELA, nome));
            st.execute();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
        } finally {
            DB.closeStatement(st);
        }

        createTriggers(nome + TABELA_VOLUME);
        createTriggers(nome + TABELA_CAPITULO);
        createTriggers(nome + TABELA_TEXTO);
        createTriggers(nome + TABELA_VOCABULARIO);
    }

    @Override
    public String selectTabela(String base) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(String.format(SELECT_LISTA_TABELAS, schema, " Table_Name = '" + base + "_volumes'"));
            rs = st.executeQuery();

            String tabela = "";
            if (rs.next())
                tabela = rs.getString("Tabela");

            return tabela;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    @Override
    public List<String> getTabelas() throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(String.format(SELECT_LISTA_TABELAS, schema, "1 > 0"));
            rs = st.executeQuery();

            List<String> list = new ArrayList<>();

            while (rs.next())
                list.add(rs.getString("Tabela"));

            return list;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

}
