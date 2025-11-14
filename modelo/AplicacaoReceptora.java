/* ***************************************************************
* Autor............: Isis Caroline Lima Viana
* Matricula........: 202410016
* Inicio...........: 16/08/2025
* Ultima alteracao.: 29/08/2025
* Nome.............: AplicacaoReceptora.java
* Funcao...........: Essa camada eh responsavel por "imprimir" e 
mostrar a mensagem decodificada na lista
*************************************************************** */
package modelo;

//importando as bibliotecas necessarias
import javafx.scene.control.Button;
//import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class AplicacaoReceptora {

  private Button botaoEnviar;
  private Button botaoVer;
  private Text textoDecodificado;
  private Text textoErro;
  private TextArea campoDeTexto;
  private ImageView imagemImpressora;
  //private Slider sliderErro;
  private CamadaAplicacaoReceptora camada_Aplicacao_Receptora;
  //private String errosImpressos;

  // Construtor:
  public AplicacaoReceptora(ImageView imagemImpressora, Button botaoEnviar, Text textoDecodificado,
  TextArea campoDeTexto, Button botaoVer, Text textoErro) {
    this.botaoEnviar = botaoEnviar;
    this.textoDecodificado = textoDecodificado;
    this.campoDeTexto = campoDeTexto;
    this.botaoVer = botaoVer;
    this.imagemImpressora = imagemImpressora;
    this.textoErro = textoErro;
  }

  public void setCamadaAplicacaoReceptora(CamadaAplicacaoReceptora camada_Aplicacao_Receptora){
    this.camada_Aplicacao_Receptora = camada_Aplicacao_Receptora;
  }

  /*
   * ***************************************************************
   * Metodo: aplicacaoReceptora
   * Funcao: mostra a mensagem codificada no Text e habilita dos botoes
   * enviar e ver, e permite que se escreva no campo de texto de novo
   * Parametros: mensagem decodificada
   * Retorno: vazio
   ****************************************************************/
  public void aplicacaoReceptora(String mensagem) {
    textoDecodificado.setText(mensagem); // mostra mensagem
    System.out.println("\nAplicacao Receptora: "+mensagem);
    animacaoImpressora();
    // habilita os botoes e o campo de texto:
    botaoEnviar.setDisable(false);
    campoDeTexto.setDisable(false);
    botaoVer.setDisable(false);
    //sliderErro.setDisable(false);
    //textoErro.setText(errosImpressos);
    botaoVer.fire(); // mostra o paneFolha com a mensagem exibida la

  }// fim do metodo AplicacaoReceptora

  /*
   * ***************************************************************
   * Metodo: animacaoImpressora
   * Funcao: troca o imageView da impressora para anima-la imprimindo
   * a mensagem
   * Parametros: nenhum
   * Retorno: vazio
   ****************************************************************/
  public void animacaoImpressora() {
    for (int i = 1; i <= 9; i++) {
      imagemImpressora.setImage(new Image("/imagens/impressora/" + i + ".png"));
      try {
        Thread.sleep(100);// pausa para a animacao ficar fluida
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    } // fim do for

  } // fim do animacaoImpressora

  public void setErro(boolean erro) {
    textoErro.setVisible(erro);
  }

  //chama o metodo parar da camada de aplicacao
  public void parar() {
    camada_Aplicacao_Receptora.parar();
  }

} // fim da classe AplicacaoReceptora