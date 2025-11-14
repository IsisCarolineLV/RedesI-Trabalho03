/* ***************************************************************
* Autor............: Isis Caroline Lima Viana
* Matricula........: 202410016
* Inicio...........: 16/09/2025
* Ultima alteracao.: 02/11/2025
* Nome.............: CamadaEnlaceDadosReceptora.java
* Funcao...........: Essa camada e responsavel por receber os quadros 
* da camada fisica. Ela implementa o controle de fluxo (enviando ACKs), 
* controle de erro (verificando paridade, CRC ou Hamming) e o 
* desenquadramento. Ao final, remonta a mensagem completa 
* ('fluxoCompleto') e a envia para a camada de aplicacao.
*************************************************************** */
package modelo;

//importando as bibliotecas necessarias
import java.util.ArrayDeque; //usada para procurar sequencia de 11111
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CamadaEnlaceDadosReceptora {
  private CamadaAplicacaoReceptora camada_Aplicacao_Receptora;
  private CamadaFisicaReceptora camada_Fisica_Receptora;
  private int tipoDeCodificacao;
  private int tipoDeEnquadramento;
  private int tipoDeControleDeErro;
  private final int contraBarra = 0b01011100, flag = 0b01111110, esc = 0b00011011, g = 0x04C11DB7; // constantes
  private ImageView balaoErro;
  private int[] fluxoCompleto = new int[0];
  private Ponteiro ponteiroFluxoTodo;
  private Temporizador t = null;
  private int num = 3;
  private Semaphore mutex = new Semaphore(1);

  // Construtor:
  public CamadaEnlaceDadosReceptora(CamadaAplicacaoReceptora camada_Aplicacao_Receptora,
      int tipoDeEnquadramento, ImageView balaoErro, int tipoDeControleDeErro, int tipoDeCodificacao) {
    this.camada_Aplicacao_Receptora = camada_Aplicacao_Receptora;
    this.tipoDeEnquadramento = tipoDeEnquadramento;
    this.balaoErro = balaoErro;
    this.tipoDeControleDeErro = tipoDeControleDeErro;
    this.tipoDeCodificacao = tipoDeCodificacao;
    ponteiroFluxoTodo = new Ponteiro(31, 0);
    ponteiroFluxoTodo.setFimQuadro(31, 0);
    // camada fisica como parametro
  }

  public void setCamadaFisicaReceptora(CamadaFisicaReceptora camada_Fisica_Receptora) {
    this.camada_Fisica_Receptora = camada_Fisica_Receptora;
  }

  /*
   * ***************************************************************
   * Metodo: CamadaEnlaceDadosReceptoraReceptora
   * Funcao: recebe o quadro enquadrado, chama o metodo de desenquadramento
   * pra ele e o encaminha para a camada de Aplicacao Receptora
   * Parametros: int[] quadro enquadrado
   * Retorno: vazio
   ****************************************************************/
  public void camadaEnlaceDadosReceptora(int quadro[]) {
    // envia quadro pro controle de fluxo (so isso)
    if (tipoDeEnquadramento != 3)
      camadaEnlaceDadosReceptoraControleDeFluxo(quadro);
    else {
      camada_Aplicacao_Receptora.camadaAplicacaoReceptora(quadro);
    }
    // envia para a proxima camada:
    // camada_Aplicacao_Receptora.camadaAplicacaoReceptora(quadroDesenquadrado);

  }// fim do metodo CamadaEnlaceDadosReceptora

  ////////////////////////////////////////////////////////////////
  /////////////////////// METODOS DE /////////////////////////////
  //////////////////// DESENQUADRAMENTO //////////////////////////
  ////////////////////////////////////////////////////////////////

  /*
   * ***************************************************************
   * Metodo: CamadaEnlaceDadosReceptoraReceptoraEnquadramento
   * Funcao: recebe o quadro enquadrado, chama o metodo de desenquadramento
   * equivalente ao tipoDeEnquadramento escolhido
   * Parametros: int[] quadro enquadrado
   * Retorno: quadroDesenquadrado
   ****************************************************************/
  public int[] camadaEnlaceDadosReceptoraEnquadramento(int quadro[]) {
    int quadroDesenquadrado[];
    switch (tipoDeEnquadramento) {
      case 0: // contagem de caracteres
        quadroDesenquadrado = camadaEnlaceDadosReceptoraEnquadramentoContagemDeCaracteres(quadro);
        break;
      case 1: // insercao de bytes
        quadroDesenquadrado = camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBytes(quadro);
        break;
      case 2: // insercao de bits
        quadroDesenquadrado = camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBits(quadro);
        break;
      case 3: // violacao da camada fisica
        quadroDesenquadrado = camadaEnlaceDadosReceptoraEnquadramentoViolacaoDaCamadaFisica(quadro);
        break;
      default:
        quadroDesenquadrado = null;
    }// fim do switch/case
    ponteiroFluxoTodo = new Ponteiro(31, 0);
    ponteiroFluxoTodo.setFimQuadro(31, 0);
    System.out.println("\nCamada de Enlace de Dados Receptora:");
    imprimir(quadroDesenquadrado);
    return quadroDesenquadrado;

  }// fim do metodo CamadaEnlaceReceptoraEnquadramento

  /*
   * ***************************************************************
   * Metodo: CamadaEnlaceDadosReceptoraContagemDeCaracteres
   * Funcao: recebe o quadro enquadrado, le o numero guardado no primeiro
   * byte e a partir desse valor n copia para o array quadroDesenquadrado
   * n os bits seguintes. Feito isso le-se o o proximo byte para se saber
   * quantos n bits ler no proximo bloco
   * Parametros: int[] quadro enquadrado
   * Retorno: quadroDesenquadrado
   ****************************************************************/
  public int[] camadaEnlaceDadosReceptoraEnquadramentoContagemDeCaracteres(int quadro[]) {
    int num = lerNumero(quadro[0]); //
    System.out.println("Contagem de caracteres: " + num);

    //Total de bytes validos
    int totalBytesEnquadrados = 0;
    if (quadro.length > 0) {
      totalBytesEnquadrados = (quadro.length - 1) * 4 + (4 - bytesVazios(quadro[quadro.length - 1]));
    }
    if (totalBytesEnquadrados == 0) {
      return new int[0]; // Quadro vazio
    }

    int[] quadroDesenquadrado = new int[quadro.length];
    int contNovoQuadro = 0, k = 32; // Ponteiros de escrita
    quadroDesenquadrado[0] = 0;

    //Ponteiros:
    int contQuadro = 0;
    int contInt = 0;
    int bytesLidos = 0; 

    try {
      while (bytesLidos < totalBytesEnquadrados) {
        // Ler o byte de cabecalho (num)
        num = 0;
        for (int j = 31; j >= 24; j--) {
          int masc = 0 | 1 << (j - contInt * 8);
          if ((masc & quadro[contQuadro]) != 0)
            num |= 1 << (j - 24);
        }
        bytesLidos++;

        // Avanca ponteiro de leitura
        contInt = (contInt + 1) % 4;
        if (contInt == 0 && bytesLidos < totalBytesEnquadrados)
          contQuadro++;

        if (num == 0)
          break; // Fim ou erro

        // Ler (num - 1) bytes 
        for (int i = 0; i < num - 1; i++) {
          if (bytesLidos >= totalBytesEnquadrados)
            break;

          // Ler o byte 
          int byteLido = 0;
          for (int j = 31; j >= 24; j--) {
            int masc = 0 | 1 << (j - contInt * 8);
            if ((masc & quadro[contQuadro]) != 0)
              byteLido |= 1 << (j - 24);
          }
          bytesLidos++;

          // Avanca ponteiro de leitura
          contInt = (contInt + 1) % 4;
          if (contInt == 0 && bytesLidos < totalBytesEnquadrados)
            contQuadro++;

          // Escrever o byte lido no novo quadro (desenquadrado)
          for (int j = 31; j >= 24; j--) { // Escreve o byte bit a bit
            k--;
            if (k < 0) {
              k = 31;
              contNovoQuadro++;
              if (contNovoQuadro >= quadroDesenquadrado.length) {
                // Se estourar o array (improvavel), pare
                throw new ArrayIndexOutOfBoundsException("Estouro de array no desenquadramento");
              }
              quadroDesenquadrado[contNovoQuadro] = 0;
            }
            int mascByte = 0 | 1 << (j - 24);
            if ((byteLido & mascByte) != 0) {
              quadroDesenquadrado[contNovoQuadro] |= 1 << k;
            }
          }
        } // Fim do loop de dados (num-1)
      } // Fim do while (bytesLidos)
    } catch (Exception a) {
      System.out.println("Erro intransponivel");
      // mensagem avisando que estourou o array:
      return new int[] { 0b01001101011001010110111001110011,
          0b01100001011001110110010101101101,
          0b00100000011011101110001101101111,
          0b00100000011001010110111001110110,
          0b01101001011000010110010001100001,
          0b00100001001000000100010101110010,
          0b01110010011011110010000001101110,
          0b01101111011100110010000001100010,
          0b01101001011100110111010000100000,
          0b01100100011001010010000001100011,
          0b01101111011011100111010001110010,
          0b01101111011011000110010100000000 };
    }

    int totalBitsEscritos = (contNovoQuadro * 32) + (32 - k);
    int tamanhoFinal = (totalBitsEscritos + 31) / 32;

    // Garante que o tamanho final nao seja zero se houver bits
    if (tamanhoFinal == 0 && totalBitsEscritos > 0)
      tamanhoFinal = 1;

    int[] quadroFinal = new int[tamanhoFinal];
    if (tamanhoFinal > 0) {
      System.arraycopy(quadroDesenquadrado, 0, quadroFinal, 0, tamanhoFinal);
    }

    return quadroFinal; // Retorna o quadro redimensionado
  }
  
  /*
   * ***************************************************************
   * Metodo: CamadaEnlaceDadosReceptoraInsercaoDeBytes
   * Funcao: recebe o quadro enquadrado, procura a flag, enquanto nao
   * a encontra vai juntando os demais bytes no caminho.
   * Parametros: int[] quadro enquadrado
   * Retorno: quadroDesenquadrado
   ****************************************************************/
  public int[] camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBytes(int quadro[]) {
    System.out.println("Insercao de Bytes");
    int totalBytes = (quadro.length * 4) - bytesVazios(quadro[quadro.length - 1]);
    int tam = (totalBytes - contarPadrao(contraBarra, quadro) + 3) / 4;
    int[] novoQuadro = new int[tam];
    boolean garanteByte = false;
    int contNovoQuadro = 0, k = 32, contInt = 0, contQuadro = 0;
    novoQuadro[0] = 0;

    while (totalBytes > 1) {
      for (int bytes = 0; bytes < 4; bytes++) {
        int byteReconhecido = 0;
        for (int j = 31; j >= 24; j--) {
          int masc = 0 | 1 << (j - contInt * 8);
          if ((masc & quadro[contQuadro]) != 0)
            byteReconhecido |= 1 << (j - 24);
        }
        if (garanteByte || (byteReconhecido != contraBarra && byteReconhecido !=esc)) {
          k -= 8;
          if (contNovoQuadro < tam)
            novoQuadro[contNovoQuadro] |= byteReconhecido << k;
          garanteByte = false;
          if (k == 0) {
            k = 32;
            contNovoQuadro++;
            if (contNovoQuadro < tam)
              novoQuadro[contNovoQuadro] = 0;
          }
        } else if (byteReconhecido == esc) {
          garanteByte = true;
        }
        contInt = (contInt + 1) % 4;
        totalBytes--;
      }
      contQuadro++;
    }
    /*
     * System.out.println("\nDESENQUADRADO:");
     * imprimir(novoQuadro);
     * System.out.println();
     */
    return novoQuadro;
  }// fim do metodo CamadaEnlaceDadosReceptoraInsercaoDeBytes

  /*
   * ***************************************************************
   * Metodo: CamadaEnlaceDadosReceptoraInsercaoDeBits
   * Funcao: recebe o quadro enquadrado, retira todas as flags e une
   * os quadros num unico bloco e, por fim chama-se o metodo tirarBits0
   * para tirar os 0 extras incluidos para proteger a mensagem
   * Parametros: int[] quadro enquadrado
   * Retorno: quadroDesenquadrado
   ****************************************************************/
  public int[] camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBits(int quadro[]) {
    // implementacao do algoritmo
    // imprimir(quadro);
    System.out.println("Insercao de Bits");
    int totalBytes = (quadro.length * 4) - bytesVazios(quadro[quadro.length - 1]);
    int tam = (totalBytes - contarPadrao(flag, quadro) + 3) / 4;
    int[] novoQuadro = new int[tam];
    int[] contQuadro = {0};
    int k = 32, contInt = 0, contNovoQuadro = 0, j=31;
    novoQuadro[0] = 0;

    while (totalBytes > 0) {
      for (int bytes = 0; bytes < 4; bytes++) {
        int byteReconhecido = 0;
        for (j=31; j >= 24; j--) {
          int masc = 0 | 1 << (j - contInt * 8);
          if ((masc & quadro[contQuadro[0]]) != 0)
            byteReconhecido |= 1 << (j - 24);
        }
        if (byteReconhecido != 0b00000000 && byteReconhecido != flag) {
          k -= 8;
          if (contNovoQuadro < tam)
            novoQuadro[contNovoQuadro] |= byteReconhecido << k;

          if (k == 0) {
            k = 32;
            contNovoQuadro++;
            if (contNovoQuadro < tam)
              novoQuadro[contNovoQuadro] = 0;
          }
        }
        contInt = (contInt + 1) % 4;
        totalBytes--;
      }
      contQuadro[0]++;
    }

    novoQuadro = tirarBits0(novoQuadro);

    // System.out.println("\nDESENQUADRADO:");
    // imprimir(novoQuadro);
    // System.out.println();

    return novoQuadro;
  }// fim do metodo CamadaEnlaceDadosReceptoraInsercaoDeBits

  /*
   * ***************************************************************
   * Metodo: CamadaEnlaceDadosReceptoraViolacaoDaCamadaFisica
   * Funcao: nao ha muito o que fazer com o quadro nessa camada entao
   * apenas retorna o quadro do jeito que ele veio, pois vai ser trabalho
   * da camada fisica lidar com o enquadramento.
   * Parametros: int[] quadro enquadrado
   * Retorno: quadroDesenquadrado
   ****************************************************************/
  public int[] camadaEnlaceDadosReceptoraEnquadramentoViolacaoDaCamadaFisica(int quadro[]) {
    // implementacao do algoritmo
    System.out.println("Violacao da camada fisica");
    return quadro;
  }// fim do metodo CamadaEnlaceDadosReceptoraViolacaoDaCamadaFisica

  ////////////////////////////////////////////////////////////////
  /////////////////////// METODOS DE /////////////////////////////
  /////////////////// CONTROLE DE ERRO ///////////////////////////
  ////////////////////////////////////////////////////////////////

  /* ***************************************************************
   * Metodo: camadaEnlaceDadosReceptoraControleDeErro
   * Funcao: Direciona o quadro recebido para o metodo de verificacao
   * de erro apropriado, com base na variavel tipoDeControleDeErro.
   * Parametros: int[] quadro recebido
   * Retorno: int[] quadro sem os bits de controle de erro
   ****************************************************************/
  public int[] camadaEnlaceDadosReceptoraControleDeErro(int quadro[]) {
    // int tipoDeControleDeErro = 0; // alterar de acordo com o teste
    switch (tipoDeControleDeErro) {
      case 0: // bit de paridade par
        System.out.println("Paridade par ("+tipoDeCodificacao+")");
        quadro = camadaEnlaceDadosReceptoraControleDeErroBitDeParidadePar(quadro);
        break;
      case 1: // bit de paridade impar
        System.out.println("Paridade impar ("+tipoDeCodificacao+")");
        quadro = camadaEnlaceDadosReceptoraControleDeErroBitDeParidadeImpar(quadro);
        break;
      case 2: // CRC
        System.out.println("CRC ("+tipoDeCodificacao+")");
        quadro = camadaEnlaceDadosReceptoraControleDeErroCRC(quadro);
        break;
      case 3: // codigo de hamming
        System.out.println("Codigo de Hamming ("+tipoDeCodificacao+")");
        quadro = camadaEnlaceDadosReceptoraControleDeErroCodigoDeHamming(quadro);
        break;
    }// fim do switch/case
    return quadro;
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErro

  /* ***************************************************************
   * Metodo: camadaEnlaceDadosReceptoraControleDeErroBitDeParidadePar
   * Funcao: Verifica a paridade par. Se a contagem de bits 1 
   * for par, remove o bit de paridade e retorna o quadro sem ele. 
   * Se for impar, detecta um erro e retorna null.
   * Parametros: int[] quadro com bit de paridade par
   * Retorno: int[] quadro sem bit de paridade (ou null)
   ****************************************************************/
  public int[] camadaEnlaceDadosReceptoraControleDeErroBitDeParidadePar(int quadro[]) {
    if (contaUns(quadro, 0) % 2 == 0) {
      Ponteiro p = identificaQuadro(quadro);
      int[] novoQuadro = new int[quadro.length];
      int j = p.intInicio() + 1;
      int[] contQuadro = { p.arrayInicio() };
      Arrays.fill(novoQuadro,0);
      while (!(j == p.intFim() && contQuadro[0] == p.arrayFim())) {
        j = decrementa(novoQuadro, contQuadro, j);
        if (((0 | 1 << j) & quadro[contQuadro[0]]) != 0)
          novoQuadro[contQuadro[0]] |= 1 << j;
      }
      // ponteiroFluxoTodo.setFimQuadro(j, j);
      imprimir(novoQuadro);
      return novoQuadro;
    } else {
      return null;
    }
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadePar

  /* ***************************************************************
   * Metodo: camadaEnlaceDadosReceptoraControleDeErroBitDeParidadeImpar
   * Funcao: Verifica a paridade impar. Se a contagem de bits 1 
   * for impar, remove o bit de paridade e retorna o quadro sem ele. 
   * Se for par, detecta um erro e retorna null.
   * Parametros: int[] quadro com bit de paridade impar
   * Retorno: int[] quadro sem bit de paridade (ou null)
   ****************************************************************/
  public int[] camadaEnlaceDadosReceptoraControleDeErroBitDeParidadeImpar(int quadro[]) {
    if (contaUns(quadro, 0) % 2 != 0) {
      Ponteiro p = identificaQuadro(quadro);
      int[] novoQuadro = new int[quadro.length];
      int j = p.intInicio() + 1;
      int[] contQuadro = { p.arrayInicio() };
      Arrays.fill(novoQuadro,0);
      while (!(j == p.intFim() && contQuadro[0] == p.arrayFim())) {
        j = decrementa(novoQuadro, contQuadro, j);
        if (((0 | 1 << j) & quadro[contQuadro[0]]) != 0)
          novoQuadro[contQuadro[0]] |= 1 << j;
      }
      // ponteiroFluxoTodo.setFimQuadro(j, j);
      imprimir(novoQuadro);
      return novoQuadro;
    } else {
      return null;
    }
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadeImpar

  /* ***************************************************************
   * Metodo: camadaEnlaceDadosReceptoraControleDeErroCRC
   * Funcao: Verifica o CRC-32. Calcula o resto sobre todo o quadro. 
   * Se o resto for 0, o quadro esta correto e o resto adiiconado eh 
   * excluido. Se for != 0, retorna null.
   * Parametros: int[] quadro
   * Retorno: int[] quadro sem resto (ou null)
   ****************************************************************/
  public int[] camadaEnlaceDadosReceptoraControleDeErroCRC(int quadro[]) {
    int resto = encontraResto(quadro);

    if (resto != 0) {
      return null; // Descarta o quadro
    }

    // Calcula o total de bits no quadro (dados + CRC)
    int totalBitsComCRC = quadro.length * 32 - (bytesVazios(quadro[quadro.length - 1]) * 8);

    // Calcula o total de bits da mensagem (sem o CRC)
    int totalBitsDeDados = totalBitsComCRC - 32;

    // Calcula o tamanho do novo array de inteiros para os dados
    int tamanhoNovoQuadro = (totalBitsDeDados + 31) / 32; // Arredonda para cima
    int[] novoQuadro = new int[tamanhoNovoQuadro];

    int[] contNovoQuadro = { 0 };
    int k = 32; // Ponteiro de bit para o novoQuadro
    novoQuadro[0] = 0;

    int contQuadro = 0; // Contador de bits lidos

    //Copia o quadro ignorando os ultimos 32
    for (int i = 0; i < quadro.length; i++) {
      for (int j = 31; j >= 0; j--) {

        if (contQuadro >= totalBitsDeDados) {
          break;
        }

        int masc = 0| 1<<j;

        k = decrementa(novoQuadro, contNovoQuadro, k); 

        // Insere o bit no novoQuadro
        if ((masc&quadro[i]) != 0) {
          novoQuadro[contNovoQuadro[0]] |= (1 << k);
        }
        contQuadro++;

      }
      if (contQuadro >= totalBitsDeDados) {
        break; 
      }
    }
    imprimir(novoQuadro);
    return novoQuadro;
  } //fim da camadaEnlaceDadosReceptoraControleDeErroCRC

  /* ***************************************************************
   * Metodo: camadaEnlaceDadosReceptoraControleDeErroCodigoDeHamming
   * Funcao: Verifica o codigo de Hamming. Recalcula os bits de 
   * paridade. Se algum estiver errado, detecta um erro e retorna 
   * 'null'. Se todos estiverem corretos, chama quadroSemPotenciasDe2 
   * para extrair os bits de controle de erro.
   * Parametros: int[] quadro 
   * Retorno: int[] quadro sem potencias de 2 (ou null)
   ****************************************************************/
  public int[] camadaEnlaceDadosReceptoraControleDeErroCodigoDeHamming(int quadro[]) {
    // implementacao do algoritmo para verificar de houve
    int f, j = 31;
    boolean achouMaiorPotenciaDe2 = false, erro = false;
    int fim = quadro.length * 32 - (bytesVazios(quadro[quadro.length - 1]) * 8);

    do {
      f = 1 << j;
      if ((fim & f) != 0)
        achouMaiorPotenciaDe2 = true;
      j--;
    } while (!achouMaiorPotenciaDe2);

    for (int n = 1; n < f; n *= 2) {
      if (contaUns(quadro, n) % 2 != 0) {
        System.out.println(n + "º ta errado");
        erro = true;
      }
    }
    quadro = quadroSemPotenciasDe2(quadro);
    if (erro) {
      System.out.println("Achou erro");
      return null;
    }
    imprimir(quadro);
    return quadro;
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErroCodigoDeHamming

  ////////////////////////////////////////////////////////////////
  /////////////////////// METODOS DE /////////////////////////////
  /////////////////// CONTROLE DE FLUXO //////////////////////////
  ////////////////////////////////////////////////////////////////

  /* ***************************************************************
   * Metodo: camadaEnlaceDadosReceptoraControleDeFluxo
   * Funcao: Gerencia o fluxo de recebimento dos quadros. Ao 
   * receber um quadro, para o temporizador que fica esperando se vai 
   * vir algum quadro ou acabou a transmissao. Chama o 
   * controle de erro. Se nao houver erro, envia um ACK, inicia um 
   * novo temporizador (para esperar o proximo quadro) e anexa os 
   * dados ao fluxoCompleto. Se houver erro, exibe o balao de aviso.
   * Parametros: int[] quadro (quadro recebido da camada fisica)
   * Retorno: void
   ****************************************************************/
  public void camadaEnlaceDadosReceptoraControleDeFluxo(int quadro[]) {
    if (t != null) {
      t.liberar();
    }
    //guarda a posicao onde o novo quadro deve comecar
    Ponteiro copia = new Ponteiro(31, 0);
    copia.setFimQuadro(ponteiroFluxoTodo.intFim(), ponteiroFluxoTodo.arrayFim());

    // verifica se deu erro:
    quadro = camadaEnlaceDadosReceptoraControleDeErro(quadro);

    if (quadro != null) { // se nao deu erro
      // cria temporizador para ver se nenhum quadro vai chegar em ate x segundos
      t = new Temporizador();
      t.start();
      // envia ACK:
      int[] ack = { 1 << 31 };
      camada_Fisica_Receptora.camadaFisicaTransmissora(ack);

      //Salva o fluxo atual
      int[] aux = fluxoCompleto;

      //Calcula quantos bits tem no novo quadro que acabou de chegar
      int bitsNoNovoQuadro = 0;
      if (quadro.length > 0) {
        // Pega o ultimo int do quadro de dados
        int ultimoInt = quadro[quadro.length - 1];
        // Conta quantos bytes vazios (do LSB para o MSB) existem nele
        int bytesVaziosNoUltimoInt = bytesVazios(ultimoInt);
        // Calcula o total de bits exatos
        bitsNoNovoQuadro = (quadro.length * 32) - (bytesVaziosNoUltimoInt * 8);
      }
      System.out.println("Bits vazios:"+bitsNoNovoQuadro);

      // Calcula o tamanho total (bits antigos + bits novos)
      int bitsAntigos = copia.tamanhoQuadro();
      int totalBits = bitsAntigos + bitsNoNovoQuadro;
      //int totalBits = copia.tamanhoQuadro() + bitsNoNovoQuadro;

      // Calcula o novo tamanho do array de int
      int tam = (totalBits + 31) / 32; // Arredonda para cima

      //para proteger o acesso ao fluxoCompleto
      try {mutex.acquire();} catch (InterruptedException e) {}

      // cria o novo array com tamanho total correto
      fluxoCompleto = new int[tam];
      Arrays.fill(fluxoCompleto, 0);

      // copia o que ficou salvo em aux para o novo fluxoCompleto
      System.arraycopy(aux, 0, fluxoCompleto, 0, aux.length);

      int k = copia.intFim() + 1; // Pega o fim do quadro anterior
      int[] contFluxo = new int[] {copia.arrayFim()};

      // copia o novo quadro
      if (bitsNoNovoQuadro > 0) {
        int bitsCopiados = 0;
        for (int i = 0; i < quadro.length; i++) {
          for (int j = 31; j >= 0; j--) {

            if (bitsCopiados >= bitsNoNovoQuadro)
              break; // Para se ja copiou tudo

            k = decrementa(fluxoCompleto, contFluxo, k); //
            int masc = 1 << j;
            if ((masc & quadro[i]) != 0) {
              fluxoCompleto[contFluxo[0]] |= 1 << k;
            }
            bitsCopiados++;
          }
          if (bitsCopiados >= bitsNoNovoQuadro)
            break;
        }
      }

      // Atualiza o ponteiro global para refletir o novo tamanho
      ponteiroFluxoTodo.add(bitsNoNovoQuadro);
      System.out.println("\nFluxo completo :");
      imprimir(fluxoCompleto);
      mutex.release();

    } else { // se deu erro
      mostrarBalaoDeErro();
    }
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeFluxo

  ////////////////////////////////////////////////////////////////
  //////////////////////// METODOS ///////////////////////////////
  ////////////////////// AUXILIARES //////////////////////////////
  ////////////////////////////////////////////////////////////////
  
  public void mostrarBalaoDeErro(){
    if (t != null && !t.liberado) {
      t.liberar();
    }
    // mostra balao de aviso de erro
    balaoErro.setImage( new Image("/imagens/balao/avisoErro.png"));
    balaoErro.setVisible(true);
    try {
      Thread.sleep(1000); // espera 1s
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    // esconde balao de aviso de erro
    balaoErro.setVisible(false);
  }

  /* ***************************************************************
   * Metodo: identificaQuadro
   * Funcao: Metodo auxiliar para identificar os limites de um quadro 
   * (inicio e fim) com base no tipo de enquadramento.
   * Parametros: int[] quadro
   * Retorno: Ponteiro 
   ****************************************************************/
  public Ponteiro identificaQuadro(int[] quadro) {
    Ponteiro p = new Ponteiro(31, 0);
    switch (tipoDeEnquadramento) {
      case 0: {
        int n = lerNumero(quadro[0]);
        p.setFimQuadro(32 - ((n * 8) % 32), (n * 8) / 32);
        break;
      }
      case 1: {
        int cont = 2, aux = 0, j = 0;
        int byteExtraido=0;
        for (j = 0; j < quadro.length; j++) {
          while (cont >= 0) {
            aux = cont;
            byteExtraido = (quadro[j] >> (cont * 8)) & 0xFF;
            // imprimir(new int[]{byteExtraido});
            // imprimir(new int[]{lido});
            if(byteExtraido == contraBarra) break;
            if (byteExtraido == esc) {
              cont--;
            }
            cont--;
          }
          if(byteExtraido == contraBarra) break;
          cont = 3;
        }
        p.setFimQuadro((aux * 8),j);
        break;
      }
      case 2: {
        int cont = 16, aux = 0, j = 0, byteExtraido = 0;
        for (j = 0; j < quadro.length; j++) {
          while ((byteExtraido ^ flag)!=0 && cont>=0) {
            aux = cont;
            byteExtraido = (quadro[j] >> (cont)) & 0xFF;
            //imprimir(new int[]{byteExtraido});
            //imprimir(new int[]{flag});
            cont--;
          }
          if((byteExtraido ^ flag)==0) break;
          cont = 31;
        }
        p.setFimQuadro(aux, j);
        //p.imp();
        break;
      }
    }
    //ponteiroFluxoTodo.add(p.tamanhoQuadro());
    return p;
  }

  /* ***************************************************************
   * Metodo: encontraResto
   * Funcao: Calcula o resto do CRC-32 (polinomio g) para o quadro. 
   * Simula a divisao polinomial bit a bit e, ao final, adiciona bits 
   * 0 ate que o resto atinja 32 bits. Se o quadro estiver correto, 
   * o resto da divisao polinomial deve ser 0
   * Parametros: int[] quadro (quadro de dados)
   * Retorno: int (o resto do CRC de 32 bits)
   ****************************************************************/
  public int encontraResto(int[] quadro) {
    int resto = 0; // Registrador CRC

    int totalBits = quadro.length * 32 - (bytesVazios(quadro[quadro.length - 1]) * 8);
    int contQuadro = 0;

    for (int i = 0; i < quadro.length; i++) {
      for (int j = 31; j >= 0 && contQuadro < totalBits; j--, contQuadro++) {

        boolean msb = (resto & 0x80000000) != 0;

        // Pega o proximo bit da mensagem
        int proximoBit = (quadro[i] >>> j) & 1;

        // Desloca o registrador para a esquerda e insere o proximo bit
        resto = (resto << 1) | proximoBit;

        // Faz o XOR com o polinomio
        if (msb) {
          resto ^= g;
        }
      } // fim do for interno
    } // fim do for externo

    // adiciona os bits 0 para completar 32 bits
    for (int k = 0; k < 32; k++) {
      // Pega o MSB
      boolean msb = (resto & 0x80000000) != 0;

      // Desloca para a esquerda
      resto <<= 1;

      // Se o MSB era 1, faz o XOR
      if (msb) {
        resto ^= g;
      }
    }

    System.out.println("RESTO (Receptor):");
    imprimir(new int[] { resto });
    return resto;
  } // fim do encontraResto

  /*
   * ***************************************************************
   * Metodo: quadroSemPotenciasDe2
   * Funcao: Faz o oposto do 'quadroComPotenciasVazias'. Ele le um
   * quadro de entrada (com dados e bits de paridade) e copia
   * apenas os bits de DADOS (pulando as posicoes 1, 2, 4, 8...)
   * para um novo quadro.
   * Parametros: int[] quadro (quadro com bits de Hamming)
   * Retorno: int[] quadroDeDados (quadro so com dados)
   ****************************************************************/
  public int[] quadroSemPotenciasDe2(int[] quadro) {
    int totalBitsNoQuadro = 0;
    if (quadro.length == 0)
      return new int[0]; // Quadro vazio
    
    totalBitsNoQuadro = (quadro.length * 32) - (bytesVazios(quadro[quadro.length - 1]) * 8);
    if (totalBitsNoQuadro == 0)
      return new int[0];

    // Descobrir quantos bits de dados teremos na saida
    int bitsDeParidade = 0;
    for (int p = 1; p <= totalBitsNoQuadro; p *= 2) {
      bitsDeParidade++;
    }
    int totalBitsDeDados = totalBitsNoQuadro - bitsDeParidade;

    if (totalBitsDeDados <= 0)
      return new int[0]; // Nao ha dados

    int tamanhoNovoQuadro = (totalBitsDeDados + 31) / 32; // Arredonda para cima
    int[] quadroDeDados = new int[tamanhoNovoQuadro];
    Arrays.fill(quadroDeDados, 0);

    int[] contQuadro = { 0 }; 
    int j = 32; 

    int[] contNovoQuadro = { 0 }; 
    int k = 32; 

    int n = 0; // contador de posicao de bit de Hamming

    // Passa bit por bit
    for (int bitIndex = 0; bitIndex < totalBitsNoQuadro; bitIndex++) {

      n++;
      j = decrementa(contQuadro, j);
      int bitLido = (quadro[contQuadro[0]] >>> j) & 1;

      // Verifica se a posicao 'n' nao eh potencia de 2
      boolean ehPotenciaDe2 = (n & (n - 1)) == 0;
      if (!ehPotenciaDe2) {
        // Escreve o bit no quadro de saida
        k = decrementa(quadroDeDados, contNovoQuadro, k);
        if (bitLido == 1) {
          quadroDeDados[contNovoQuadro[0]] |= (1 << k);
        }
      }
      // Se FOR potencia de 2, o bitLido e simplesmente ignorado/descartado.
    }

    return quadroDeDados;
  } //fim do quadroSemPotenciasDe2

  /* ***************************************************************
   * Metodo: contaUns
   * Funcao: Conta o numero de bits 1 em todo o quadro. Se k > 0,
   * e usado para Hamming: conta bits 1 apenas nas posicoes n 
   * onde (k & n) == k.
   * Parametros: int[] quadro, int k (potencia de 2 ou 0 para contagem total).
   * Retorno: int contagem de bits 1
   ****************************************************************/
  public int contaUns(int[] quadro, int k) {
    int cont = 0, j = 32, n = 1;
    int bitFinal = quadro.length * 32 - bytesVazios(quadro[quadro.length - 1]) * 8;
    // System.out.println("Bit final: "+bitFinal);
    int[] i = { 0 };
    while (true) {
      j = decrementa(i, j);
      int masc = 0 | 1 << j;
      if ((k & n) == k) {// 100 & 101
        if ((masc & quadro[i[0]]) != 0) {
          cont++;
        }
      }
      if ((n) == bitFinal)
        break;
      n++;
    }
    return cont;
  } //fim do contaUns

  /* ***************************************************************
   * Metodo: decrementa (com int[] quadro)
   * Funcao: Decrementa um ponteiro de bit. Se k < 0, 
   * reseta para 31 e avanca o ponteiro de array (contQuadro) e 
   * zera a nova posicao do array quadro. (Usado para escrita).
   * Parametros: int[] quadro (array sendo escrito), int[] contQuadro 
   * (ponteiro de indice), int k (ponteiro de bit).
   * Retorno: int (o novo valor de 'k')
   ****************************************************************/
  public int decrementa(int[] quadro, int[] contQuadro, int k) {
    k--;
    if (k < 0) {
      k = 31;
      contQuadro[0]++;
      if (contQuadro[0] < quadro.length)
        quadro[contQuadro[0]] = 0;
    }
    return k;
  } //fim do decrementa

  /* ***************************************************************
   * Metodo: decrementa (sem int[] quadro)
   * Funcao: Decrementa um ponteiro de bit . Se k < 0, 
   * reseta para 31 e avanca o ponteiro de array (contQuadro). 
   * (Usado para leitura, nao modifica o array).
   * Parametros: int[] contQuadro (ponteiro de indice), int k 
   * (ponteiro de bit).
   * Retorno: int (o novo valor de k)
   ****************************************************************/
  public int decrementa(int[] contQuadro, int k) {
    k--;
    if (k < 0) {
      k = 31;
      contQuadro[0]++;
    }
    return k;
  } //fim do decrementa

  /*public int bitsVazios(int[] quadro) {
    int ultimoInt = 0, b = 0;
    while (ultimoInt == 0) {
      b++;
      ultimoInt = quadro[quadro.length - b];
    }
    int i;
    for (i = 0; i < 31; i++) {
      int mascara = 0 | 1 << i;
      if ((mascara & ultimoInt) != 0)
        break;
    }
    System.out.println("Ultimo bit em " + (32 - i));
    return (b - 1) * 32 + i;
  } //fim bitsVazios*/

  /* ***************************************************************
   * Metodo: bytesVazios
   * Funcao: Conta quantos bytes vazios tem num int
   * Parametros: int ultimoInt (o inteiro a ser verificado)
   * Retorno: int (numero de bytes vazios, de 0 a 3)
   ****************************************************************/
  public int bytesVazios(int ultimoInt) {
    int i = 0;
    for (i = 0; i < 4; i++) {
      int mascara = 0;
      mascara = 0b11111111 << (i * 8);
      if ((mascara & ultimoInt) != 0)
        break;
    }
    return i;
  } //fim do bytesVazios

  /* ***************************************************************
   * Metodo: lerNumero
   * Funcao: Le os primeiros 8 bits (o primeiro byte) de um 
   * inteiro e o retorna como um numero
   * Parametros: int primeiroInt 
   * Retorno: int (o valor do primeiro byte)
   ****************************************************************/
  public int lerNumero(int primeiroInt) {
    int num = 0;
    for (int i = 31; i >= 24; i--) {
      int masc = 0 | 1 << i;
      if ((masc & primeiroInt) != 0)
        num |= 1 << (i - 24);
    }
    return num;
  }

  /* ***************************************************************
   * Metodo: contarPadrao
   * Funcao: conta quantas vezes o padrao apareceu no quadro
   * Parametros: int padrao (o byte a ser procurado), int[] quadro
   * Retorno: int
   ****************************************************************/
  public int contarPadrao(int padrao, int[] quadro) {
    int cont = 0;
    for (int i = 0; i < quadro.length; i++) {
      for (int j = 0; j < 4; j++) {
        int byteReconhecido = 0;
        for (int k = 31; k >= 24; k--) {
          int masc = 1 << (k - (j * 8));
          if ((masc & quadro[i]) != 0)
            byteReconhecido |= 1 << (k - 24);
        }
        if (byteReconhecido == padrao) {
          cont++;
          j++;
        }
      }
    }
    // System.out.println("Contador de flags:"+cont);
    return cont;
  }

 /* ***************************************************************
   * Metodo: buscarUns
   * Funcao: conta quantos 0 deverao ser inseridos (quantas sequencias 
   * 11111 a mensagem tem)
   * Parametros: int[] quadro
   * Retorno: int (a contagem de sequencias "11111")
   ****************************************************************/
  public int buscarUns(int[] quadro) {
    int cont = 0;
    ArrayDeque<Boolean> sequenciaBits = new ArrayDeque<>(5);
    for (int i = 0; i < quadro.length; i++) {
      for (int j = 31; j >= 0; j--) {
        if (sequenciaBits.size() == 5)
          sequenciaBits.removeFirst();
        int masc = 1 << j;
        if ((masc & quadro[i]) != 0) {
          sequenciaBits.addLast(true);
        } else {
          sequenciaBits.addLast(false);
        }
        if (sequenciaBits.size() == 5 && sequenciaBits.stream().allMatch(b -> b)) {
          cont++;
          sequenciaBits.clear(); // ou continue monitorando
        }
      }
    }
    return cont;
  }

  /* ***************************************************************
   * Metodo: tirarBits0
   * Funcao: quando ve 111110 tira esse 0 para retornar a mensagem 
   * original
   * Parametros: int[] quadro
   * Retorno: int[] quadro sem os bits 0 de stuffing
   ****************************************************************/
  // 
  public int[] tirarBits0(int[] quadro) {
    int tamEmBits = (quadro.length * 32) - buscarUns(quadro);
    int tam = (tamEmBits + 31) / 32; // arredonda pra cima
    int[] quadroSemZero = new int[tam];
    int k = 32, contQ = 0;

    quadroSemZero[0] = 0;

    ArrayDeque<Boolean> sequenciaBits = new ArrayDeque<>(5);
    for (int i = 0; i < quadro.length; i++) {
      for (int j = 31; j >= 0; j--) {
        k--;
        if (k < 0) {
          k = 31;
          contQ++;
          if (contQ < tam)
            quadroSemZero[contQ] = 0;
          else
            break;
        }
        if (sequenciaBits.size() == 5)
          sequenciaBits.removeFirst();
        int masc = 1 << j;
        if ((masc & quadro[i]) != 0) {
          sequenciaBits.addLast(true);
          quadroSemZero[contQ] |= 1 << k;
        } else {
          sequenciaBits.addLast(false);
        }
        if (sequenciaBits.size() == 5 && sequenciaBits.stream().allMatch(b -> b)) {
          sequenciaBits.clear(); // ou continue monitorando
          j--;
        }
      }
    }
    return quadroSemZero;
  }

 /* ***************************************************************
   * Metodo: parar
   * Funcao: chama o metodo parar da camada fisica, libera o temporizador
   * que esperava o novo quadro e reseta o ponteiro e o array fluxoCompleto
   * Parametros: void
   * Retorno: void
   ****************************************************************/
  public void parar() {
    if (t != null) {
      t.liberar();
    } // joga fora o timer que estiver ativo
    ponteiroFluxoTodo = new Ponteiro(31, 0);
    ponteiroFluxoTodo.setFimQuadro(31, 0);
    fluxoCompleto = new int[0];
    camada_Fisica_Receptora.parar();
  }

  /* ***************************************************************
   * Metodo: avisaErro
   * Funcao: Repassa um aviso de erro para a camada de aplicacao 
   * receptora.
   * Parametros: void
   * Retorno: void
   ****************************************************************/
  public void avisaErro() {
    camada_Aplicacao_Receptora.avisaErro();
  }

  ////////////////////////////////////////////////////////////////
  ////////////////////// METODOS DE //////////////////////////////
  ////////////////////// IMPRESSAO ///////////////////////////////
  ////////////////////////////////////////////////////////////////

  public void imprimir(int[] vetor) {
    for (int a : vetor) {
      String bits32 = String.format("%32s", Integer.toBinaryString(a)).replace(' ', '0');
      System.out.println(bits32);
    }
  }

  ////////////////////////////////////////////////////////////////
  ////////////////////// TEMPORIZADOR ////////////////////////////
  ////////////////////////////////////////////////////////////////
  private class Temporizador extends Thread {
    protected boolean liberado = false;
    private Image imagemBalao;
    private Image imagemErro;
    // private Semaphore mutex = new Semaphore(1);

    public Temporizador() {
      this.setDaemon(true);
      imagemBalao = new Image("/imagens/balao/ret1.png");
      imagemErro = new Image("/imagens/balao/avisoErro.png");
    }

    /* ***************************************************************
     * Metodo: run
     * Funcao: Espera por um tempo. Se nenhum quadro chegar, assume que a 
     * transmissao terminou. Ele entao manda o fluxoCompleto para a
     * camada de desenquadramento e depois para a camada de aplicacao.
     * Parametros: void
     * Retorno: void
     ****************************************************************/
    public void run() {
      balaoErro.setImage(imagemBalao);
      // espera o tempo suficiente para enviar o ack e receber o proximo quadro
      for (int i = 0; i < 100; i++) {
        try {
          sleep(50 * num); // vai esperar (2.2*tamanho dos quadros) segundos
          if(i>30){
            balaoErro.setVisible(true);
          }
        } catch (InterruptedException e) {
          break;
        }
        if (liberado) { // se chegou o proximo quadro, pode fechar o temporizador
          balaoErro.setVisible(false);
          break;
        }
      } // fim da espera

      balaoErro.setVisible(false);
      // se acabou o tempo e nenhum quadro chegou
      if (!liberado) {
        System.out.println("Fim da transmissao");
        try {
          mutex.acquire();
        } catch (InterruptedException e) {
        }
        fluxoCompleto = camadaEnlaceDadosReceptoraEnquadramento(fluxoCompleto);
        int[] quadro = fluxoCompleto;
        fluxoCompleto = new int[0];
        mutex.release();
        camada_Aplicacao_Receptora.camadaAplicacaoReceptora(quadro);
      }
      balaoErro.setImage(imagemErro);
    }

    /* ***************************************************************
     * Metodo: liberar
     * Funcao: Define a flag liberado como true e interrompe 
     * a thread, sinalizando que um novo quadro chegou.
     * Parametros: void
     * Retorno: void
     ****************************************************************/
    public void liberar() {
      liberado = true;
      this.interrupt();
    }
  } // fim da classe Temporizador

} // fim da classe CamadaEnlaceDadosReceptora