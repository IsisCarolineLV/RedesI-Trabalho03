/* ***************************************************************
* Autor............: Isis Caroline Lima Viana
* Matricula........: 202410016
* Inicio...........: 16/08/2025
* Ultima alteracao.: 13/10/2025
* Nome.............: ControllerTela.java
* Funcao...........: Essa classe controla a interface javaFX, controlando
a visibilidade dos panes, definindo os metodos que reagirao aos eventos do 
botoes e criando a referencia para cada elemento da interface.Alem disso,
nessa classe sao instanciados cada uma das camadas envolvidas nessa 
simulacao. 
*************************************************************** */
package controle;

//importando as bibliotecas necessarias
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import modelo.*;

public class ControllerTela {

  @FXML
  private Text textoErros;

  @FXML
  private Label labelEnquadramento;

  @FXML
  private Label labelCodificacao;

  @FXML
  private Label labelControleErro;
  
  @FXML
  private Label labelERRO;  //para mostrar a taxa de erro na tela
  
  @FXML
  private Slider sliderErro;  //para definir a taxa de erro na mensagem

  @FXML
  private ProgressBar barraDeProgresso;

  //Botoes:
  @FXML
  private Button botaoEnviar;

  @FXML
  private Button botaoSair;

  @FXML
  private Button botaoSairMenu;

  @FXML
  private Button botaoSairFolha;  //esconde o paneFolha

  @FXML
  private Button botaoVer;  //mostra o paneFolha

  @FXML
  private Button botaoVoltar; //retorna para o menu
  
  @FXML
  private Button botaoIniciar; //esconde o menu e
                               //inicia a simulacao

  //Panes:
  @FXML
  private AnchorPane paneFolha; //onde mostra o texto decodificado

  @FXML
  private AnchorPane paneMenu;

  @FXML
  private AnchorPane panePrincipal;
  
  @FXML
  private AnchorPane paneSinal;  //mostra o sinal

  //Elementos de texto:
  @FXML
  private TextArea campoDeTexto;//onde pode-se digitar a mensagem 
  
  @FXML
  private Text textoDecodificado;//onde a mensagem decodificada sera exibida

  //ComboBox:
  @FXML
  private ComboBox<String> comboBox;  //para selecionar o tipo de codificacao
  
  @FXML
  private ComboBox<String> comboBoxEnquadramento;  //para selecionar o tipo de enquadramento

  @FXML
  private ComboBox<String> comboBoxControle; //para selecionar o tipo de controle de erro

  //ImageViews:
  @FXML
  private ImageView imagemBalaoErro;

  @FXML
  private ImageView imagemFolha;  //a mensagem decodificada eh exibida em cima dessa imagem

  @FXML
  private ImageView imagemFundo;

  @FXML
  private ImageView imagemImpressora;

  @FXML
  private ImageView imagemMenu; //fundo do menu

    //ImageViews do sinal binario:
  @FXML
  private ImageView imagemSinal1;

  @FXML
  private ImageView imagemSinal2;

  @FXML
  private ImageView imagemSinal3;

  @FXML
  private ImageView imagemSinal4;

  @FXML
  private ImageView imagemSinal5;

  @FXML
  private ImageView imagemSinal6;

  @FXML
  private ImageView imagemSinal7;

  @FXML
  private ImageView imagemSinal8;
  
  @FXML
  private ImageView imagemSinal9;

  @FXML
  private ImageView imagemSinal10;

  @FXML
  private ImageView imagemSinal11;

  @FXML
  private ImageView imagemSinal12;

  @FXML
  private ImageView imagemSinal13;

  @FXML
  private ImageView imagemSinal14;

  @FXML
  private ImageView imagemSinal15;

  @FXML
  private ImageView imagemSinal16;

  @FXML
  private ImageView imagemTransicao1;

  @FXML
  private ImageView imagemTransicao2;

  @FXML
  private ImageView imagemTransicao3;

  @FXML
  private ImageView imagemTransicao4;

  @FXML
  private ImageView imagemTransicao5;

  @FXML
  private ImageView imagemTransicao6;

  @FXML
  private ImageView imagemTransicao7;

  @FXML
  private ImageView imagemTransicao8;

  @FXML
  private ImageView imagemTransicao9;

  @FXML
  private ImageView imagemTransicao10;

  @FXML
  private ImageView imagemTransicao11;

  @FXML
  private ImageView imagemTransicao12;

  @FXML
  private ImageView imagemTransicao13;

  @FXML
  private ImageView imagemTransicao14;

  @FXML
  private ImageView imagemTransicao15;


  //Cria as images que serao exibidas nos imageViews logo no comeco
  //para que nao seja preciso instancia-las muitas e muitas vezes:
  private Image[] imagens = new Image[10];
  //Arrays dos imageViews dos sinais (para facilitar sua manipulacao)
  private ImageView[] imagensDoSinal;
  private ImageView[] imagensTransicoes;
  
  private int tipoDeCodificacao = 5;  //salva a codificacao escolhida no comboBox
  private int tipoDeEnquadramento = 5; //salva o enquadramento escolhido
  private int tipoDeControleDeErro =5;
  private int taxaDeErro = 0;

  //eh preciso chamar o primeiro metodo a partir dessa camada
  private AplicacaoTransmissora aplicacao_Transmissora; 
  private AplicacaoReceptora aplicacao_Receptora;

  /* ***************************************************************
   * Metodo: initialize
   * Funcao: chama os metodos para todos os recursos necessarios para
   * que a logica do programa (os arrays de imagem, os listeners do 
   * campo de texto e do comboBox, estilizar a barra de progresso,etc)
   * Parametros: nenhum
   * Retorno: vazio
   * ****************************************************************/
  public void initialize() {
    criarArraysDeImagens();
    botaoEnviar.setDisable(true); //enquanto nada estiver escrito o botao fica desabilitado
    criarListeners();
    barraDeProgresso.setStyle("-fx-accent: #80d9a8;");  //pintando a barra de verde
    campoDeTexto.setText(""); //limpando o campoDeTexto
  } //fim do initialize

  /* ***************************************************************
   * Metodo: criarArraysDeImagens
   * Funcao: coloca as imagens e imageViews em seus respectivos arrays
   * Parametros: nenhum
   * Retorno: vazio
   * ****************************************************************/
  public void criarArraysDeImagens() {
    //Array de imagens:
    imagens[0] = new Image("/imagens/tela1.png");
    imagens[1] = new Image("/imagens/telaSair.png");
    imagens[2] = new Image("/imagens/telaVoltar.png");
    imagens[3] = new Image("/imagens/telaMenu.png");
    imagens[4] = new Image("/imagens/telaMenu.png");
    imagens[5] = new Image("/imagens/telaMenuSair.png");
    imagens[6] = new Image("/imagens/folha.png");
    imagens[7] = new Image("/imagens/folhaSair.png");
    imagens[8] = new Image("/imagens/impressora/verPagina.png");
    imagens[9] = new Image("/imagens/impressora/1.png");
    
    //Array de imageView dos sinais:
    imagensDoSinal = new ImageView[]{imagemSinal1,imagemSinal2,imagemSinal3,
                                    imagemSinal4,imagemSinal5,imagemSinal6,
                                    imagemSinal7,imagemSinal8,imagemSinal9,
                                    imagemSinal10,imagemSinal11,imagemSinal12,
                                    imagemSinal13,imagemSinal14,imagemSinal15,
                                    imagemSinal16};
    
    //Array de imageView das transicoes:
    imagensTransicoes = new ImageView[]{imagemTransicao1,imagemTransicao2,imagemTransicao3,
                                    imagemTransicao4,imagemTransicao5,imagemTransicao6,
                                    imagemTransicao7,imagemTransicao8,imagemTransicao9,
                                    imagemTransicao10,imagemTransicao11,imagemTransicao12,
                                    imagemTransicao13,imagemTransicao14,imagemTransicao15};
 
  } //fim do criarArraysDeImagens

  /* ***************************************************************
   * Metodo: criarListeners
   * Funcao: cria os listeners para o campo de texto e para o comboBox
   * Parametros: nenhum
   * Retorno: vazio
   * ****************************************************************/
  public void criarListeners() {
    // listener para ativar o botao enviar quando houver algo escrito no campo:
    campoDeTexto.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String textoAntigo, String textoNovo) {
        botaoEnviar.setDisable(textoNovo.equals("") || textoNovo==null);
        paneFolha.setVisible(false);
      }
    });

    //listener para limitar o tamanho da mensagem para que ela nao ultrapasse o 12 linhas:
    campoDeTexto.setTextFormatter(new TextFormatter<String>(change -> {
      if (change.isContentChange()) {
        String novoTexto = change.getControlNewText(); // retorna o texto
        int quebrasDeLinha = (int) (novoTexto.chars().filter(ch -> ch == '\n').count() + 1); // verifica quantas quebras
                                                                                             // de linha teve
        quebrasDeLinha += (int) (novoTexto.length() / 12);
        if (novoTexto.length() > 180 || quebrasDeLinha > 13) {
          return null; // ignora a digitação extra
        }
      }
      return change;
    }));
    
    //setando as opcoes do comboBox
    comboBox.getItems().addAll("Binario",
        "Manchester",
        "Manchester Diferencial");
    // criando o listener do comboBox de codificacao
    comboBox.setOnAction(e -> {
      String selecionado = comboBox.getValue();
      labelCodificacao.setText(selecionado);
      //salva o valor lido na variavel tipoDeCodificacao:
      tipoDeCodificacao = comboBox.getItems().indexOf(selecionado);
      if(tipoDeEnquadramento==3 && tipoDeCodificacao==0)
        botaoIniciar.setDisable(true);
      else if(comboBoxEnquadramento.getValue()!=null && comboBoxControle.getValue()!=null)
        botaoIniciar.setDisable(false);
    }); // fim do listener
    
    comboBoxEnquadramento.getItems().addAll("Contagem de Caracteres",
        "Insercao de Bytes",
        "Insercao de Bits",
        "Violacao da Camada Fisica");
    // criando o listener do comboBox de enquadramento
    comboBoxEnquadramento.setOnAction(e -> {
      String selecionado = comboBoxEnquadramento.getValue();
      labelEnquadramento.setText(selecionado);
      //salva o valor lido na variavel tipoDeEnquadramento:
      tipoDeEnquadramento = comboBoxEnquadramento.getItems().indexOf(selecionado);
      if(tipoDeEnquadramento==3 && tipoDeCodificacao==0)
        botaoIniciar.setDisable(true);
      else if(comboBox.getValue()!=null && comboBoxControle.getValue()!=null)
        botaoIniciar.setDisable(false);
    }); // fim do listener

    //setando as opcoes do comboBox de Controle de Erros
    comboBoxControle.getItems().addAll("Paridade Par",
        "Paridade Impar",
        "CRC",
        "Codigo de Hamming");
    // criando o listener do comboBox de codificacao
    comboBoxControle.setOnAction(e -> {
      String selecionado = comboBoxControle.getValue();
      labelControleErro.setText(selecionado);
      //salva o valor lido na variavel tipoDeCodificacao:
      tipoDeControleDeErro = comboBoxControle.getItems().indexOf(selecionado);
      if(tipoDeCodificacao==0 && tipoDeEnquadramento==3)
        botaoIniciar.setDisable(true);
      else if(comboBoxEnquadramento.getValue()!=null && comboBox.getValue()!=null)
        botaoIniciar.setDisable(false);
    }); // fim do listener
    
    sliderErro.valueProperty().addListener((observable, oldValue, newValue) -> {
      taxaDeErro = (int) Math.round(newValue.doubleValue())*10;
      labelERRO.setText(taxaDeErro+"%");
      aplicacao_Transmissora.setTaxaDeErro(taxaDeErro);
    });
    
  } // fim do criarListeners

  /* ***************************************************************
   * Metodo: criarCamadas
   * Funcao: instancia as camadas e relaciona-as umas com as outras
   * Parametros: nenhum
   * Retorno: vazio
   * ****************************************************************/
  public void criarCamadas(){

    // RECEPTOR:
    aplicacao_Receptora = new AplicacaoReceptora(imagemImpressora, botaoEnviar, 
    textoDecodificado, campoDeTexto, botaoVer, textoErros);

    CamadaAplicacaoReceptora camada_Aplicacao_Receptora = new CamadaAplicacaoReceptora(aplicacao_Receptora);

    CamadaEnlaceDadosReceptora camada_EnlaceDados_Receptora = 
    new CamadaEnlaceDadosReceptora(camada_Aplicacao_Receptora, tipoDeEnquadramento, imagemBalaoErro, 
      tipoDeControleDeErro, tipoDeCodificacao);

    CamadaFisicaReceptora camada_Fisica_Receptora = new CamadaFisicaReceptora(camada_EnlaceDados_Receptora,
        tipoDeCodificacao, tipoDeEnquadramento);
    
    //MEIO DE COMUNICACAO:
    MeioDeComunicacao meioDeComunicacao = new MeioDeComunicacao(tipoDeCodificacao, 
    camada_Fisica_Receptora, imagensDoSinal, barraDeProgresso, imagensTransicoes,
    0, tipoDeEnquadramento);

    // TRANSMISSOR:
    CamadaFisicaTransmissora camada_Fisica_Transmissora = new CamadaFisicaTransmissora(meioDeComunicacao,
        tipoDeCodificacao, tipoDeEnquadramento);
    CamadaEnlaceDadosTransmissora camada_EnlaceDados_Transmissora = 
    new CamadaEnlaceDadosTransmissora(camada_Fisica_Transmissora, tipoDeEnquadramento, tipoDeControleDeErro); //ida

    CamadaAplicacaoTransmissora camada_Aplicacao_Transmissora = 
    new CamadaAplicacaoTransmissora(camada_EnlaceDados_Transmissora);

    aplicacao_Transmissora = new AplicacaoTransmissora(campoDeTexto, camada_Aplicacao_Transmissora);

    //No sentido contrario
    camada_Fisica_Transmissora.setCamadaEnlaceDadosTransmissora(camada_EnlaceDados_Transmissora); //volta
    meioDeComunicacao.setTransmissor(camada_Fisica_Transmissora); //volta do meio de comunicacao
    camada_Fisica_Receptora.setMeioDeComunicacao(meioDeComunicacao);
    camada_EnlaceDados_Receptora.setCamadaFisicaReceptora(camada_Fisica_Receptora);
    camada_Aplicacao_Receptora.setCamadaEnlaceDadosReceptora(camada_EnlaceDados_Receptora);
    aplicacao_Receptora.setCamadaAplicacaoReceptora(camada_Aplicacao_Receptora);

    meioDeComunicacao.start();
  } //fim do criarCamadas
  
  ////////////////// METODOS QUE RESPONDEM AOS EVENTOS DOS BOTOES ///////////////////

  /* ***************************************************************
   * Metodo: clicouEnviar
   * Funcao: desabilita os botoes de enviar e ver a mensagem, esconde
   * o paneFolha e chama a aplicacaoTransmissora
   * Parametros: ActionEvent (botao clicado)
   * Retorno: vazio
   * ****************************************************************/
  @FXML
  void clicouEnviar(ActionEvent event) {
    paneFolha.setVisible(false);
    botaoEnviar.setDisable(true);
    botaoVer.setDisable(true);
    aplicacao_Transmissora.aplicacaoTransmissora();
    aplicacao_Receptora.setErro(false);
    //sliderErro.setDisable(true);
  } //fim do clicouEnviar

  /* ***************************************************************
   * Metodo: clicouSair
   * Funcao: fecha a aplicacao
   * Parametros: ActionEvent (botao clicado)
   * Retorno: vazio
   * ****************************************************************/
  @FXML
  void clicouSair(ActionEvent event) {
    Platform.exit(); // fecha a aplicacao
  } //fim do clicouSair

  /* ***************************************************************
   * Metodo: clicouVoltar
   * Funcao: mata a Thread e retorna todos os elementos para seus estados
   * iniciais e deixa o paneMenu visivel novamente
   * Parametros: ActionEvent (botao clicado)
   * Retorno: vazio
   * ****************************************************************/
  @FXML
  void clicouVoltar(ActionEvent event) {
    aplicacao_Transmissora.parar();
    aplicacao_Receptora.parar();
    paneMenu.setVisible(true);
    campoDeTexto.setText("");
    botaoVer.setDisable(true);
    botaoEnviar.setDisable(false);
    campoDeTexto.setDisable(false);
    textoDecodificado.setText("");
    paneFolha.setVisible(false);
    sliderErro.setDisable(false);
    sliderErro.setValue(0);
    aplicacao_Receptora.setErro(false);
  } //fim do clicouVoltar

  /* ***************************************************************
   * Metodo: clicouSairFolha
   * Funcao: esconde o paneFolha
   * Parametros: ActionEvent (botao clicado)
   * Retorno: vazio
   * ****************************************************************/
  @FXML
  void clicouSairFolha(ActionEvent event) {
    paneFolha.setVisible(false);
  } //fim do clicouSairFolha

  /* ***************************************************************
   * Metodo: clicouVerFolha
   * Funcao: mostra o paneFolha
   * Parametros: ActionEvent (botao clicado)
   * Retorno: vazio
   * ****************************************************************/
  @FXML
  void clicouVerFolha(ActionEvent event) {
    paneFolha.setVisible(true);
  } //fim do clicouVerFolha

  /* ***************************************************************
   * Metodo: clicouIniciar
   * Funcao: esconde o paneMenu e instancia as camadas
   * Parametros: ActionEvent (botao clicado)
   * Retorno: vazio
   * ****************************************************************/
  @FXML
  void clicouIniciar(ActionEvent event) {
    paneMenu.setVisible(false);
    criarCamadas();
  } //fim do clicouIniciar

  ////////////////// METODOS PURAMENTE ESTETICOS ///////////////////

  // Esse metodo aumenta o tamanho do botao para sinalizar que ele eh clicavel
  @FXML
  void destacarBotao(MouseEvent event) {
    Button botao = (Button) event.getSource(); // referencia qual botao clicou
    if (botao.getId().equals("botaoSair")) {
      imagemFundo.setImage(imagens[1]);
    } else if (botao.getId().equals("botaoVoltar")) {
      imagemFundo.setImage(imagens[2]);
    } else if (botao.getId().equals("botaoEnviar")) {
      botaoEnviar.setScaleX(1.1);
      botaoEnviar.setScaleY(1.1);
    } else if (botao.getId().equals("botaoVer")) {
      imagemImpressora.setImage(imagens[8]);
    } else if (botao.getId().equals("botaoSairMenu")) {
      imagemMenu.setImage(imagens[5]);
    } else if (botao.getId().equals("botaoSairFolha")) {
      imagemFolha.setImage(imagens[7]);
    } else if (botao.getId().equals("botaoIniciar")) {
      botaoIniciar.setScaleX(1.1);
      botaoIniciar.setScaleY(1.1);
    }
  } //fim do destacarBotao

  // retorna o botao destacado para seu tamanho normal
  @FXML
  void voltaBotao(MouseEvent event) {
    Button botao = (Button) event.getSource(); // referencia qual botao gerou o evento
    if (botao.getId().equals("botaoEnviar")) {
      botaoEnviar.setScaleX(1);
      botaoEnviar.setScaleY(1);
    }else if (botao.getId().equals("botaoIniciar")) {
      botaoIniciar.setScaleX(1);
      botaoIniciar.setScaleY(1);
    } else if (botao.getId().equals("botaoVer")) {
      imagemImpressora.setImage(imagens[9]);
    } else if (botao.getId().equals("botaoSairMenu")) {
      imagemMenu.setImage(imagens[3]);
    } else if (botao.getId().equals("botaoSairFolha")) {
      imagemFolha.setImage(imagens[6]);
    } else {
      imagemFundo.setImage(imagens[0]);
    }
  } //fim do voltaBotao

} //fim da classe ControllerTela
