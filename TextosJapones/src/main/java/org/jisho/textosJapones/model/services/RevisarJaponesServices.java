package org.jisho.textosJapones.model.services;

import org.jisho.textosJapones.database.dao.DaoFactory;
import org.jisho.textosJapones.database.dao.RevisarDao;
import org.jisho.textosJapones.database.dao.VocabularioDao;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.entities.VocabularioExterno;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

import java.util.List;
import java.util.UUID;

public class RevisarJaponesServices {

	private final RevisarDao revisarDao = DaoFactory.createRevisarJaponesDao();
	private final List<VocabularioDao> externos = DaoFactory.getVocabularioExternos();

	private void updateExterno(Revisar revisar) throws ExcessaoBd {
		VocabularioExterno vocabulario = new VocabularioExterno(revisar.getId(), revisar.getVocabulario(), revisar.getPortugues(), revisar.getIngles(), revisar.getLeitura(), revisar.getLeituraNovel(), revisar.getRevisado().isSelected());
		for (VocabularioDao dao : externos)
			dao.update(vocabulario);
	}

	public List<Revisar> selectAll() throws ExcessaoBd {
		return revisarDao.selectAll();
	}

	public List<Revisar> selectTraduzir(Integer quantidadeRegistros) throws ExcessaoBd {
		return revisarDao.selectTraduzir(quantidadeRegistros);
	}

	public RevisarJaponesServices insertOrUpdate(List<Revisar> lista) throws ExcessaoBd {
		for (Revisar obj : lista)
			insertOrUpdate(obj);

		return this;
	}

	public RevisarJaponesServices insertOrUpdate(Revisar obj) throws ExcessaoBd {
		if (revisarDao.exist(obj.getVocabulario()))
			revisarDao.update(obj);
		else
			insert(obj);

		updateExterno(obj);
		return this;
	}

	public RevisarJaponesServices insert(Revisar obj) throws ExcessaoBd {
		if (obj.getId() == null)
			obj.setId(UUID.randomUUID());
		revisarDao.insert(obj);
		return this;
	}

	public RevisarJaponesServices insert(List<Revisar> lista) throws ExcessaoBd {
		for (Revisar obj : lista)
			insert(obj);

		return this;
	}

	public void update(Revisar obj) throws ExcessaoBd {
		revisarDao.update(obj);
		updateExterno(obj);
	}

	public void delete(Revisar obj) throws ExcessaoBd {
		revisarDao.delete(obj);
	}

	public void delete(String vocabulario) throws ExcessaoBd {
		revisarDao.delete(vocabulario);
	}

	public void delete(List<Revisar> lista) throws ExcessaoBd {
		for (Revisar obj : lista)
			delete(obj);
	}

	public Revisar select(String vocabulario, String base) throws ExcessaoBd {
		return revisarDao.select(vocabulario, base);
	}

	public Revisar select(String vocabulario) throws ExcessaoBd {
		return revisarDao.select(vocabulario);
	}

	public boolean existe(String palavra) throws ExcessaoBd {
		return revisarDao.exist(palavra);
	}

	public List<String> selectFrases(String select) throws ExcessaoBd {
		return revisarDao.selectFrases(select);
	}

	public String selectQuantidadeRestante() throws ExcessaoBd {
		return revisarDao.selectQuantidadeRestante();
	}

	public Revisar selectRevisar(String pesquisar, Boolean isAnime, Boolean isManga, Boolean isNovel) throws ExcessaoBd {
		return revisarDao.selectRevisar(pesquisar, isAnime, isManga, isNovel);
	}

	public List<Revisar> selectSimilar(String vocabulario, String ingles) throws ExcessaoBd {
		return revisarDao.selectSimilar(vocabulario, ingles);
	}

	public void incrementaVezesAparece(String vocabulario) throws ExcessaoBd {
		revisarDao.incrementaVezesAparece(vocabulario);
	}

	public void setIsManga(Revisar obj) throws ExcessaoBd {
		revisarDao.setIsManga(obj);
	}

	public void setIsNovel(Revisar obj) throws ExcessaoBd {
		revisarDao.setIsNovel(obj);
	}
}
