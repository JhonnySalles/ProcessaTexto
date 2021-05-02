# Processa Texto Japonês
> Programa para processamento de legendas de anime e textos japonês, extraindo os vocabulários do texto com sua tradução.

<h4 align="center"> 
	🛰  Versão 0.0.5
</h4>

[![Build Status][travis-image]][travis-url]

Programa para processamento de frases em japonês ou de legendas de anime previamente inserida no banco de dados, utiliza-se a api Sudachi para a extração do vocabulário e consulta no banco de dados para obtenção das traduções das palavras encontrada.


<p align="center">
 <a href="#Sobre">Sobre</a> •
 <a href="#Instruções-de-uso">Instruções de uso</a> • 
 <a href="#Ambiente-de-Desenvolvimento">Ambiente de Desenvolvimento</a> • 
 <a href="#Histórico-de-Release">Histórico de Release</a> • 
 <a href="#Features">Features</a> • 
 <a href="#Meta">Meta</a> • 
 <a href="#Contribuindo">Contribuindo</a> • 
 <a href="#Licença">Licença</a>
</p>

## Sobre

Projetado para trabalhar gerando o vocabulário de palavras em japonês e a consulta em um dicionário próprio do significado no banco de dados, também irá gerar novos registro no dicionário quando não encontrado, utiliza método de exclusão para palavras já conhecida e familiar do usuário, podendo ser adicionado a qualquer momento.

Posteriormente foi adaptado para estar trabalhando com SELECT em alguma tabela ou base no banco de dados para que gere o vocabulário de várias frases, base esta que foi projetado para ser gerado através de legendas extraida com o programa [subs2srs](http://subs2srs.sourceforge.net/) que gera deck para o anki.

## Instruções de uso

**Tela Vocabulário**

A tela foi criada para auxliar na criação de cartões para o [anki](https://apps.ankiweb.net/), onde extrai dos textos informados o vocabulário e então realiza uma consulta tanto no dicionário interno do programa, como também de 3 principais sites de dicionário Japonês.
Sendo os sites o [Jisho](https://jisho.org/), [Tanoshii japonese](https://www.tanoshiijapanese.com/) e [Japan dict](https://www.japandict.com/).
Também utiliza o [Google Translate](https://translate.google.com.br/?hl=pt-BR) para a tradução dos significados do inglês para o português.

* Texto

![image](https://user-images.githubusercontent.com/43912638/116819829-3891b400-ab48-11eb-91ff-6287051504a4.png)


O formato foi implementado para auxiliar na criação de cartões de vocabulário no anki como pode ser observado na imagem acima.
A primeira linha irá ser considerado a palavra do vocabulário e ela será excluida da analise das linhas subsequente.
No exemplo acima existe 3 frases que irão ser processada, no qual gerou o vocabulário de duas delas. Na primeira frase não foi identificada vocabulário, pois o kanji 上 está incluso como exclusão por ser de fácil memorização.
A maior parte do vocabulário encontra já existia no dicionário interno, então apenas adicionaram ela a lista e seu significado.
Um dos vocabulário não foi encontrado e então foi adicionado na grid para revisão, com seu significado obtido dos sites de dicionário e traduzido do inglês para o português pelo google tradutor.
A palavra nova ficou marcado no texto de destido, assim que realizar a correção e salvar no dicionário interno será então substituida pelo significado corrigido.

* Música

![image](https://user-images.githubusercontent.com/43912638/116817354-4b05f080-ab3c-11eb-93b8-0fc1e30bf01f.png)

Para o formato de música, o programa analisa linha a linha gerando o vocabulário, ele leva em consideração que cada linha é uma frase diferente, onde no campo de destino irá ter a linha correspondente com seu vocabulário logo abaixo.
Para palavras novas ocorre o mesmo processo anterior, sendo adicionado na grid para revisão.
O campo exclusão apresenta abaixo uma breve lista do vocabulário que deve ser ignorado ao analisar as sentenças.


**Tela Legenda**

A tela possui várias abas para o processamento das legendas que estejam em alguma tabela ou base do banco de dados MySQL, ela foi projetada para funcionar informando os comandos do banco de dados para facilitar o processamento de diversos registros de uma vez, tendo em vista que o banco de dados é semelhante a um excel e seus comandos facilitam a utilização.

* Importar legenda

A aba foi criada com a finalidade de importar novos vocabulário de um SELECT, sendo substituida posteriormente por todo o processo da aba Gerar vocabulário.
Ela apenas realizava a importação para a tabela temporária os vocabulário que não existiam internamente no dicionário, para que pudesse ser revisada na aba Traduzir vocabulário.

* Traduzir vocabulário

![image](https://user-images.githubusercontent.com/43912638/116818232-b5b92b00-ab40-11eb-8a3a-955760679f2c.png)

A aba possui a finalidade de procurar um significado para os novos vocabulários nos sites de dicionário e realizar a tradução do seu significado com o recurso de tradução automática utilizando o Google Translate.
Nela também é possível fazer a revisão dos registros, antes de ser inserido no dicionário interno.

* Gerar vocabulário
    
![image](https://user-images.githubusercontent.com/43912638/116817489-fadb5e00-ab3c-11eb-9acb-9feaee82e5d4.png)

A aba possui a finalidade de realizar o processamento das legendas que estejam no banco de dados, utiliza-se de comandos do MySQL para encontrar os registros a ser processado.
Logo abaixo possui uma grid para a prévia de todos os registros que estão sendo processado.

É necessário informar um **SELECT**, um **UPDATE** e um **DELETE** para realizar o processamento.
  O **SELECT** deve possuir dois campos, sendo um o primeiro o id do campo no banco de dados e o segundo o texto a ser processado.
  O **UPDATE** deve informar para qual campo no banco o vocabulário extraido vai e em seu WHERE deve se encontrar o campo id da tabela.
  O **DELETE** é utilizado para realizar uma limpeza na coluna de vocabulario, onde em uma nova execução pode ser necessário limpar os vocabulários antigos para que os registos que não possuem mais vocabulário devido a novas inserções de exclusões do vocabulário, possam ser limpas.

![image](https://user-images.githubusercontent.com/43912638/116817885-f44de600-ab3e-11eb-9fab-5c9e1553775a.png)

Após processado os registros, pode ser feito uma conferência antes de realizar o UPDATE no banco, sendo exibido o vocabulário gerado ao lado de cada frase.

Como os comandos são executados indvidualmente, também foi implementado dois novos botões "Salvar fila" e "Executar fila", onde o processo executado acima pode ser salva no banco para posteriormente ser executado.
Porém diferente da execução manual, a fila executa todos os 3 comandos em ordem para cada registro na fila.

A execução ocorre na seguinte sequência:
1. Realiza o SELECT.
2. Faz o processamento dos registros.
3. Faz a limpeza da base com o DELETE.
4. Realiza o UPDATE.
5. Segue para o próximo item da fila.

* Revisar vocabulário
    
![image](https://user-images.githubusercontent.com/43912638/116817612-9a005580-ab3d-11eb-97f0-4184d8ab8489.png)

Esta aba realiza a correção do vocabulário obtido dos sites de dicionário e que foram traduzido automáticamente na aba de tradução.
A ordenação dos registros será dos de maior para o menor na frequência de aparecimento nas extrações de vocabulário.
Ela irá mostrar o significado do vocabulário encontrada no idioma inglês e a tradução realizada pelo google tradutor.
Também servirá para correções de revisões realizadas anteriormente.

## Ambiente de Desenvolvimento

Necessário realizar préviamente a instalação do [java na versão 15](https://www.java.com/pt-BR/).
Necessário a utilização do banco de dados [MySQL](https://www.mysql.com/). 
Pode-se utilizar qualquer IDE de desenvolvimento de sua escolha, recomenda-se a versão 2020-12 do [eclipse](https://www.eclipse.org/downloads/).
Utilizado no projeto o mavem para controle e gerenciamento das dependências.
O projeto utiliza da api de inteligência artificial [Sudachi](https://github.com/WorksApplications/Sudachi) para o reconhecimento e a extração do vocabulário em Japonês. Também está preparado para funcionar nos 3 modos disponíveis.
Incluso o recurso de visualização do progresso no ícone da bandeja do sistema, graças ao componente [FXTaskbarProgressBar](https://github.com/Dansoftowner/FXTaskbarProgressBar), que realiza a conexão com o windows para visualização do progresso em segundo plano.

## Histórico de Release

* 0.0.1
    * Implementado a comunicação com o banco de dados MySQL.
    * Adicionado o Sudachi ao projeto para extração do vocabulário.
    * Extração de vocabulário de frases.
    * Exclusão de palavras conhecidas.
    * Geração e restauração de backup da base.
    * Correção do vocabulário salvo.
    * Criado nova função para extração de vocabulario de música e textos longos.
* 0.0.2
    * Nova tela para extração de vocabulário atraves de SELECT.
    * Atualização para o java 14.
    * Criado tela para gerar estatísticas de leitura do Kanji.
    * Adicionado consulta ao site Jisho e JapanDict para vocabulário novo.
    * Adicionado tradução do significado do vocabulário novo para o português com o Google Translate.
    * Criado tela para processamento de legendas de anime em um banco de dados extraido com o programa subs2srs.
    * Processamento de SELECT em lote, onde utiliza uma fila no banco de dados para os processamentos.
* 0.0.3
    * Correções de erros no projeto.
    * Implementado a consulta do vocabulário ao site Tanoshii.
    * Nova funcionalidade para salvar vocabulário não corrigido extraido dos site de consulta em uma tabela temporária.
* 0.0.4
    * Correção de erros no projeto.
    * Atualização do banco de dados.
* 0.0.5
    * Criação de tela para correção do vocabulário temporário.
    * Ajuste para melhor funcionalidade das telas.

### Features

- [X] Extração de vocabulários de frases em japonês
- [X] Extração de vocabulários de textos em japonês
- [X] Exclusões de palavras conhecida ou familiares 
- [X] Salvar novos vocabulários
- [X] Correções de vocabulário
- [X] Criação de tabela de estatísticas de kanji para o anki
- [X] Gerar e restaurar backup do banco de dados
- [X] Processamento de registros no banco de dados
- [X] Processamento de registros no banco de dados em lote

## Meta

Distribuido sobre a licença GPL. Veja o arquivo ``COPYING`` para maiores informações.
[https://github.com/JhonnySalles/github-link](https://github.com/JhonnySalles/ProcessaTextoJapones/blob/master/COPYING)

## Contribuindo

1. Fork (<https://github.com/JhonnySalles/ProcessaTextoJapones/fork>)
2. Crie sua branch de recurso (`git checkout -b feature/fooBar`)
3. Faça o commit com suas alterações (`git commit -am 'Add some fooBar'`)
4. Realize o push de sua branch (`git push origin feature/fooBar`)
5. Crie um novo Pull Request

<!-- Markdown link & img dfn's -->

## Licença

[GPL-3.0 License](https://github.com/JhonnySalles/ProcessaTextoJapones/blob/master/COPYING)
