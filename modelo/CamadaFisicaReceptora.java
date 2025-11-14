/* ***************************************************************
* Autor............: Isis Caroline Lima Viana
* Matricula........: 202410016
* Inicio...........: 16/08/2025
* Ultima alteracao.: 02/11/2025
* Nome.............: CamadaFisicaReceptora.java
* Funcao...........: Essa camada eh responsavel por decodificar o 
sinal recebido em binario comum a partir da codificacao escolhida 
(binario, manchester ou manchester diferencial) e enviar a mensagem 
escolhida para a CamadaAplicacaoReceptora Para essa nova adicao do 
ack+temporizador eh preciso que tanto o transmissor quanto o receptor 
possam enviar e receber mensagens. Por isso, essa classe tambem 
possuira os metodos de decodificacao
****************************************************************/
package modelo;

public class CamadaFisicaReceptora {

  private CamadaEnlaceDadosReceptora camada_Enlace_Dados_Receptora;
  private int tipoDeDecodificacao;
  private int tipoDeEnquadramento;
  private MeioDeComunicacao meioDeComunicacao;
  //private MeioDeComunicacao copiaMeioDeComunicacao;

  // Construtor
  public CamadaFisicaReceptora(CamadaEnlaceDadosReceptora camada_Enlace_Dados_Receptora, int tipoDeDecodificacao,
      int tipoDeEnquadramento) {
    this.camada_Enlace_Dados_Receptora = camada_Enlace_Dados_Receptora;
    this.tipoDeDecodificacao = tipoDeDecodificacao;
    this.tipoDeEnquadramento = tipoDeEnquadramento;
    // fazer animacao da impressora
  }

  public void setMeioDeComunicacao(MeioDeComunicacao meioDeComunicacao){
    this.meioDeComunicacao = meioDeComunicacao;
  }


  /////////////////////////////////////////////////////////////////
  ////////////////////// METODOS DA ///////////////////////////////
  //////////////// CAMADA FISICA RECEPTORA ////////////////////////
  /////////////////////////////////////////////////////////////////

  /* ***************************************************************
   * Metodo: camadaFisicaReceptora
   * Funcao: verifica qual a codificacao escolhida e chama o metodo
   * equivalente a essa escolha, por fim envia o fluxoBrutoDeBits
   * codificados para a camadaEnlaceDadosReceptora
   * Parametros: int[] (mensagem codificada no tipo escolhido)
   * Retorno: vazio
   ****************************************************************/
  public void camadaFisicaReceptora(int quadro[]) {
    int fluxoBrutoDeBits[] = new int[(int) quadro.length / 2];
    switch (tipoDeDecodificacao) {
      case 0: // codificao binaria
        fluxoBrutoDeBits = camadaFisicaReceptoraDecodificacaoBinaria(quadro);
        break;
      case 1: // codificacao manchester
        if (tipoDeEnquadramento != 3) {
          fluxoBrutoDeBits = camadaFisicaReceptoraDecodificacaoManchester(quadro);
        } else {
          fluxoBrutoDeBits = violacaoDaCamadaFisica(quadro);
        }
        break;
      case 2: // codificacao manchester diferencial
        if (tipoDeEnquadramento != 3) {
          fluxoBrutoDeBits = camadaFisicaReceptoraDecodificacaoManchesterDiferencial(quadro);
        } else {
          fluxoBrutoDeBits = violacaoDaCamadaFisica(quadro);
        }
        break;
    }// fim do switch/case
     // chama proxima camada
     if(fluxoBrutoDeBits!=null){
      System.out.println("\nCamada Fisica Receptora:\n"+imprimirVetor(fluxoBrutoDeBits));
      camada_Enlace_Dados_Receptora.camadaEnlaceDadosReceptora(fluxoBrutoDeBits);
     }
  }// fim do metodo CamadaFisicaTransmissora

  /* ***************************************************************
   * Metodo: camadaFisicaReceptoraDecodificacaoBinaria
   * Funcao: a mensagem ja esta em AscII binario, entao apenas se
   * retorna o quadro enviado como parametro
   * Parametros: int[] (mensagem codificada em binario)
   * Retorno: int[] (mensagem codificada em binario)
   ****************************************************************/
  public int[] camadaFisicaReceptoraDecodificacaoBinaria(int quadro[]) {
    return quadro;
  }// fim do metodo CamadaFisicaReceptoraDecodificacaoBinaria

  /* ***************************************************************
   * Metodo: camadaFisicaReceptoraDecodificacaoManchester
   * Funcao: verifica os bits de 2 em 2, se achar a sequencia 10
   * escreve 1 na posicao (iteracao) em que ele foi achado no Array
   * quadroDecodificado
   * Parametros: int[] (mensagem codificada em manchester)
   * Retorno: int[] (mensagem codificada em binario)
   ****************************************************************/
  public int[] camadaFisicaReceptoraDecodificacaoManchester(int quadro[]) {
    int indexQuadro = 0, k;
    int tam = (quadro.length + 2 - 1) / 2; // arredonda pra cima
    int[] quadroDecodificado = new int[tam];
    //System.out.println(imprimirBinario(quadro[0]));
    for (int i = 0; i < quadroDecodificado.length; i++) {
      k = 31;
      quadroDecodificado[i] = 0; // zera todas as posicoes
      for (int j = 31; j >= 0; j--) {

        if (j == 15) {
          indexQuadro++;
          k = 31;
        } // cada quadroDecodificado[] guarda 4 caracteres
        // entao na metade k volta pro final
        if (indexQuadro >= quadro.length)
          break;
        // se o par for 10 entao escreve um 1
        if (((quadro[indexQuadro] & 1 << k) != 0) && ((quadro[indexQuadro] & 1 << (k - 1)) == 0))
          quadroDecodificado[i] = (quadroDecodificado[i] | 1 << j);
        else if ((((quadro[indexQuadro] & 1 << k) != 0) && ((quadro[indexQuadro] & 1 << (k - 1)) != 0)) ||
                (((quadro[indexQuadro] & 1 << k) == 0) && ((quadro[indexQuadro] & 1 << (k - 1)) == 0)) ){
                camada_Enlace_Dados_Receptora.mostrarBalaoDeErro();
                return null;
                }
          //quadroDecodificado[i] = (quadroDecodificado[i] | 1 << j); // esse da erro, mas nao sei como sinalizar
        k -= 2; // verifica o proximo par
      }
      indexQuadro++;
    }
    return quadroDecodificado;
  }// fim do metodo CamadaFisicaReceptoraDecodificacaoManchester

  /* ***************************************************************
   * Metodo: CamadaFisicaReceptoraDecodificacaoManchesterDiferencial
   * Funcao: verifica os bits de 2 em 2, se ouve mudanca de um bit
   * para o outro escreve 0, se nao escreve 1
   * Parametros: int[] (mensagem codificada em manchester diferencial)
   * Retorno: int[] (mensagem codificada em binario)
   ****************************************************************/
  public int[] camadaFisicaReceptoraDecodificacaoManchesterDiferencial(int quadro[]) {

    boolean estadoAnterior = false; // to usando boolean pq int ia embolar tudo 0=false e 1=true
    int indexQuadro = 0, k = 31;
    int tam = (quadro.length + 2 - 1) / 2; // arredonda pra cima
    int[] quadroDecodificado = new int[tam];

    for (int i = 0; i < quadro.length; i++) {

      for (int j = 31; j >= 0; j -= 2) {
        int a = (quadro[i] & 1 << j);
        int b = (quadro[i] & 1 << (j - 1));
        if(((a == 0) && (b == 0)) && ((j+1)%8==0) && ((quadro[i] & 11 << j-3)==0)){
          break;
        }else if (((a != 0) && (b != 0)) || ((a == 0) && (b == 0))) {
          camada_Enlace_Dados_Receptora.mostrarBalaoDeErro();
          return null;
        }
        int mascara = 1 << j;
        boolean bitNovo = (quadro[i] & mascara) != 0;

        if (estadoAnterior != bitNovo) {
          quadroDecodificado[indexQuadro] |= 1 << k;
        }
        estadoAnterior = bitNovo;
        k--;
        if (k < 0) {
          k = 31;
          indexQuadro++;
        }
      }
    }
    return quadroDecodificado;
  }// fim do CamadaFisicaReceptoraDecodificacaoManchesterDiferencial

  /* ***************************************************************
   * Metodo: violacaoDaCamadaFisica
   * Funcao: metodo de desenquadramento que envolve identificar os quadros
   * com dois pares altos (11) no inicio e no fim. Vai copiando os bits do 
   * quadro enquadrado ate terminar todo o fluxo de bits
   * Parametros: int[] quadro (enquadrado)
   * Retorno: int[] quadroDesenquadrado
   ****************************************************************/
  public int[] violacaoDaCamadaFisica(int[] quadro) {
    //System.out.println("ORIGINAL:"+imprimirVetor(quadro));
    int num=7;  //tamanho do quadro
    int totalBytesCodificados = quadro.length*2-bytesVaziosManchester(quadro[quadro.length-1]);
    int totalPares = totalBytesCodificados*8;
    int tam = (totalBytesCodificados + 1) / 2; // arredonda pra cima
    int[] quadroDecodificado = new int[tam];
    int k=32, contDecofificado=0; //auxiliares do quadro decofificado
    int ponteiro=29, contQuadro=0;  //auxiliares do quadro original 
    int d=31, contD=0;  //auxiliares dos quadros individuais
    int[] quadroD = new int[(num+3)/4];
    quadroD[0]=0;

    totalPares--; //tira o primeiro 11

    while(totalPares>0){
      int a = (quadro[contQuadro] & 1 << ponteiro)==0?0:1;
      int b = (quadro[contQuadro] & 1 << (ponteiro - 1))==0?0:1;
      if(a==1 && b==1){
        //insere o quadro decodificado num unico bloco:
        for(int i=0; i<quadroD.length; i++){
          for(int j=31; j>=0; j-=2){
            int bit1 = (quadroD[i] >> j) & 1;
            int bit2 = (quadroD[i] >> (j-1)) & 1;
            k--;
            if(k<0){
              k=31;
              contDecofificado++;
              if(contDecofificado<quadroDecodificado.length)
                quadroDecodificado[contDecofificado]=0;
            }
            if (bit1!=0)
              quadroDecodificado[contDecofificado] |= 1<<k;
            k--;
            if (bit2!=0)
              quadroDecodificado[contDecofificado] |= 1<<k;
            if (bit1 == 0 && bit2 == 0) {k+=2;break;}
          }
        } //fim do inserir quadroD em quadroDecodificado
        quadroD = new int[(num+3)/4];
        quadroD[0]=0;
        d=31;
      } //fim do if fechou o quadro
      else{
        if(a==1)
          quadroD[0] |= 1<<d;
        if(b==1)
          quadroD[0] |= 1<<(d-1);
        d-=2;
        if(d<0){
          d=31;
          contD++;
          if(contD<quadroD.length)
            quadroD[contD]=0;
        }
      }
      ponteiro-=2;
      if(ponteiro<0) {
        ponteiro=31;
        contQuadro++;
      }
      totalPares--;
    } //fim do loop

    if (d != 31 || contD > 0) { // Se ha dados no último quadroD
      for (int i = 0; i <= contD; i++) {
        for (int j = 31; j > 0; j -= 2) {
          if (k < 0) break;
          
          int bit1 = (quadroD[i] >> j) & 1;
          int bit2 = (quadroD[i] >> (j-1)) & 1;
          
          // Para no 00 legitimo
          if (bit1 == 0 && bit2 == 0) break;
          
          // Adiciona bit1
          k--;
          if (bit1 == 1) {
            quadroDecodificado[contDecofificado] |= (1 << k);
          }
          
          // Adiciona bit2  
          k--;
          if (bit2 == 1) {
            quadroDecodificado[contDecofificado] |= (1 << k);
          }
          
          if (k < 0) {
            k = 31;
            contDecofificado++;
            if (contDecofificado >= quadroDecodificado.length) break;
            quadroDecodificado[contDecofificado] = 0;
          }
        }
      }
    }
   
    /*System.out.println(("Antes da traducao:"));
    System.out.println(imprimirVetor(quadroDecodificado));
    System.out.println("AAA");*/
    if(tipoDeDecodificacao==1)
      quadroDecodificado = camadaFisicaReceptoraDecodificacaoManchester(quadroDecodificado);
    else if(tipoDeDecodificacao==2)
      quadroDecodificado = camadaFisicaReceptoraDecodificacaoManchesterDiferencial(quadroDecodificado);
    return quadroDecodificado;
  }


  /////////////////////////////////////////////////////////////////
  //////////////////////// METODOS DA /////////////////////////////
  //////////////// CAMADA FISICA TRANSMISSORA /////////////////////
  /////////////////////////////////////////////////////////////////
  
  /* ***************************************************************
   * Metodo: camadaFisicaTransmissora
   * Funcao: verifica qual a codificacao escolhida e chama o metodo
   * equivalente a essa escolha, por fim envia o fluxoBrutoDeBits
   * codificados para o meioDeComunicacao
   * Parametros: int[] (mensagem em AscII binario)
   * Retorno: vazio
   ****************************************************************/
  public void camadaFisicaTransmissora(int quadro[]) {
    // tipoDeCodificacao = 2; // alterar de acordo o teste
    int fluxoBrutoDeBits[] = new int[quadro.length];
    switch (tipoDeDecodificacao) {
      case 0: // codificao binaria
        fluxoBrutoDeBits = camadaFisicaTransmissoraCodificacaoBinaria(quadro);
        break;
      case 1: // codificacao manchester
        if(tipoDeEnquadramento!=3){
          fluxoBrutoDeBits = camadaFisicaTransmissoraCodificacaoManchester(quadro);
        }else{
        fluxoBrutoDeBits = violacaoDaCamadaFisica(
            camadaFisicaTransmissoraCodificacaoManchester(quadro));
        }
        break;
      case 2: // codificacao manchester diferencial
        if(tipoDeEnquadramento!=3){
          fluxoBrutoDeBits = camadaFisicaTransmissoraCodificacaoManchesterDiferencial(quadro);
        }else{
        fluxoBrutoDeBits = violacaoDaCamadaFisica(
            camadaFisicaTransmissoraCodificacaoManchesterDiferencial(quadro));
        }
        break;
    }// fim do switch/case
    meioDeComunicacao.comunicar(fluxoBrutoDeBits, false);
  }// fim do metodo CamadaFisicaTransmissora

  /* ***************************************************************
   * Metodo: camadaFisicaTransmissoraCodificacaoBinaria
   * Funcao: nao muda nada no quadro original entao apenas o retorna
   * Parametros: int[] (mensagem em AscII binario)
   * Retorno: int[] (quadro codificado)
   ****************************************************************/
  public int[] camadaFisicaTransmissoraCodificacaoBinaria(int quadro[]) {
    return quadro;
  }// fim do metodo CamadaFisicaTransmissoraCodificacaoBinaria

  /* ***************************************************************
   * Metodo: camadaFisicaTransmissoraCodificacaoManchester
   * Funcao: para cada 0 escreve 01 e para cada 1 escreve 10
   * Parametros: int[] (mensagem em AscII binario)
   * Retorno: int[] (quadro codificado em Manchester)
   ****************************************************************/
  public int[] camadaFisicaTransmissoraCodificacaoManchester(int quadro[]) {
    int bytesValidos = 4 - bytesVazios(quadro[quadro.length - 1]);
    int[] quadroManchester;
    if (bytesValidos > 2)
      quadroManchester = new int[quadro.length * 2];
    else
      quadroManchester = new int[(quadro.length * 2) - 1];
    int cont = 0, k = 31, contBit = 0;
    int bitsValidos = 32 * (quadro.length - 1) + 8 * bytesValidos;

    for (int i = 0; i < quadro.length; i++) {
      k = 31;
      quadroManchester[cont] = 0;
      for (int j = 31; j >= 0; j--) {
        contBit++;

        if (j == 15) {
          cont++;
          quadroManchester[cont] = 0;
        }
        int mascara = 1 << j;
        if ((mascara & quadro[i]) != 0) { // se for 1 escreve 10
          quadroManchester[cont] = (quadroManchester[cont] | 1 << k);
          k -= 2; // pula a posicao do '1' e do '0'

        } else { // se for 0 escreve 01
          k--; // pula a posicao do '0'
          quadroManchester[cont] = (quadroManchester[cont] | 1 << k);
          k--; // pula a posicao do '1'
        }
        if (contBit == bitsValidos)
          break;
      }
      cont++;
    }

    return quadroManchester;
  }// fim do metodo CamadaFisicaTransmissoraCodificacaoManchester

  /* ***************************************************************
   * Metodo: camadaFisicaTransmissoraCodificacaoManchesterDiferencial
   * Funcao: a variavel booleana sinalAtual representa a polaridade
   * do sinal, quando ele eh verdadeiro escreve 10, quando ele eh falso
   * escreve 01. Antes de escrever esses valores no novo array, porem,
   * caso ele veja um 0 o valor verdade de sinalAtual eh invertido e
   * caso ele veja 1, o sinalAtual permanece o mesmo
   * Parametros: int[] (mensagem em AscII binario)
   * Retorno: int[] (quadro codificado em Manchester Diferencial)
   ****************************************************************/
  public int[] camadaFisicaTransmissoraCodificacaoManchesterDiferencial(int quadro[]) {
    int bytesValidos = 4 - bytesVazios(quadro[quadro.length - 1]);
    int[] quadroManchesterDiferencial;
    if (bytesValidos > 2)
      quadroManchesterDiferencial = new int[quadro.length * 2];
    else
      quadroManchesterDiferencial = new int[(quadro.length * 2) - 1];
    int bitsValidos = 32 * (quadro.length - 1) + 8 * bytesValidos;
    boolean sinalAtual = false; // Comeca com nivel baixo
    int k, indexQuadroMD = 0, contBits = 0;
    for (int i = 0; i < quadro.length; i++) {
      quadroManchesterDiferencial[indexQuadroMD] = 0;
      k = 31;
      for (int j = 31; j >= 0; j--) {
        contBits++;
        if (j == 15) {
          indexQuadroMD++;
          k = 31;
        }
        int mascara = 1 << j;
        boolean novoBit = (mascara & quadro[i]) != 0; // eh 1

        /*
         * se foi zero trocou vai ter mudado entao ele vai imprimir o
         * oposto do que estava antes. Ja se nao foi 0, entao trocou segue
         * com o mesmo valor verdade, portanto o sinal mantem seu fluxo:
         */
        // Manchester Diferencial: bit 0 = transicao, bit 1 = mantem
        if (novoBit) { // Se for 1, inverte
          sinalAtual = !sinalAtual;
        }
        if (!sinalAtual) { // se for 0 escreve 01
          k--; // pula a posicao do '0'
          quadroManchesterDiferencial[indexQuadroMD] |= 1 << k;
          k--; // pula a posicao do '1'

        } else { // se for 1 escreve 10
          quadroManchesterDiferencial[indexQuadroMD] |= 1 << k;
          k -= 2; // pula a posicao do '1' e do '0'
        }
        if (contBits == bitsValidos)
          break;
      } // fim do for interno (passando pelos 31 bits do inteiro
      indexQuadroMD++;
    } // fim do for externo (passando por cada inteiro do array)

    // transforma o sinal codificado para codificacao manchester
    return quadroManchesterDiferencial;
  }// fim do CamadaFisicaTransmissoraCodificacaoManchesterDiferencial

  /* ***************************************************************
   * Metodo: parar
   * Funcao: chama o metodo para matar a Copia do MeioDeComunicacao
   * Parametros: nenhum
   * Retorno: vazio
   ****************************************************************/
  public void parar() {
    meioDeComunicacao.matarThread();
  } // fim do metodo parar

  public void avisaErro(){
    camada_Enlace_Dados_Receptora.avisaErro();
  }

  ////////////////////////////////////////////////////////////////
  //////////////////////// METODOS ///////////////////////////////
  ////////////////////// AUXILIARES //////////////////////////////
  ////////////////////////////////////////////////////////////////
  public int bytesVazios(int ultimoInt) {
    int i = 0;
    for (i = 0; i < 4; i++) {
      int mascara = 0;
      mascara = 0b11111111 << (i * 8);
      if ((mascara & ultimoInt) != 0)
        break;
    }
    return i;
  }

  public int bytesVaziosManchester(int ultimoInt) {
    int i = 0;
    for (i = 0; i < 2; i++) {
      int mascara = 0;
      mascara = 0b1111111111111111 << (i * 16);
      if ((mascara & ultimoInt) != 0)
        break;
    }
    return i;
  }
  

  ////////////////////////////////////////////////////////////////
  ////////////////////// METODOS DE //////////////////////////////
  ////////////////////// IMPRESSAO ///////////////////////////////
  ////////////////////////////////////////////////////////////////

  public String imprimirBinario(int teste) {
    String a = "";
    a += String.format("%32s", Integer.toBinaryString(teste)).replace(' ', '0');
    a += "\n";
    return a;
  }

  public String imprimirVetor(int[] vetor) {
    String a = "";
    for (int i : vetor) {
      a += imprimirBinario(i);
    }
    return a;
  }

} // fim da classe CamadaFisicaReceptora