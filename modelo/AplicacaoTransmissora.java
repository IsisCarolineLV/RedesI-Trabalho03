/* ***************************************************************
* Autor............: Isis Caroline Lima Viana
* Matricula........: 202410016
* Inicio...........: 16/08/2025
* Ultima alteracao.: 28/08/2025
* Nome.............: AplicacaoTransmissora.java
* Funcao...........: Essa camada eh responsavel por ler o que esta 
escrito no campo de texto quando se clica no botaoEnviar e envia-la
para a camada de aplicacao transmissora
*************************************************************** */
package modelo;

import javafx.scene.control.TextArea;

public class AplicacaoTransmissora{
  
  private TextArea campoDeTexto;
  private CamadaAplicacaoTransmissora camada_Aplicacao_Transmissora;
  
  //Construtor:
  public AplicacaoTransmissora(TextArea campoDeTexto, CamadaAplicacaoTransmissora camada_Aplicacao_Transmissora){
    this.campoDeTexto = campoDeTexto;
    this.camada_Aplicacao_Transmissora = camada_Aplicacao_Transmissora;
  }
  
  /* ***************************************************************
   * Metodo: aplicacaoTransmissora
   * Funcao: pega a mensagem do campo de texto e a envia para a 
   * camada de Aplicacao Transmissora
   * Parametros: nenhum
   * Retorno: vazio
   * ****************************************************************/
  public void aplicacaoTransmissora () {
    String mensagem = campoDeTexto.getText();
    campoDeTexto.setDisable(true);
    System.out.println("Aplicacao Transmissora: "+mensagem);
    //chama a proxima camada:
    camada_Aplicacao_Transmissora.camadaAplicacaoTransmissora(mensagem); //em um exemplo mais realistico, aqui seria dado um SEND do SOCKET
  }//fim do metodo AplicacaoTransmissora
  
  /* ***************************************************************
   * Metodo: parar
   * Funcao: pega a mensagem do campo de texto e a envia para a 
   * camada de Aplicacao Transmissora
   * Parametros: nenhum
   * Retorno: vazio
   * ****************************************************************/
  public void parar(){
    camada_Aplicacao_Transmissora.parar();  //vai chamar o metodo parar ate a camada fisica transmissora
  }

  public void setTaxaDeErro(int taxa){
    camada_Aplicacao_Transmissora.setTaxaDeErro(taxa);
  }
  
} //fim da classe AplicacaoTransmissora