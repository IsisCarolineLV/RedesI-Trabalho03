# Terceiro Trabalho – Redes I

Este repositório contém a implementação do **Terceiro Trabalho da matéria Redes I**.

**Nota:** 90/100

**Professor:** Marlos Marques  
**Semestre:** 2025.2

---

## Descrição

Este trabalho é uma continuação do Segundo Trabalho de Redes I.

O objetivo é implementar controle de erro e controle de fluxo utilizando o protocolo Stop-and-Wait, aplicados sobre o enquadramento do fluxo de bits.

A implementação simula o envio de quadros do transmissor para o receptor, incluindo:

* Detecção de erros na transmissão
     * Bit de Paridade Par e Ímpar
     * CRC
     * Código de Hamming

* Confirmação de recebimento (ACK)

* Reenvio de quadros em caso de erro ou perda

* Controle do fluxo de dados por meio do mecanismo Stop-and-Wait

---

## Para executar o trabalho

Tenha o **Java 8** instalado.

### Verificar se o Java está instalado corretamente:

```bash
java -version
javac -version
```

Deve aparecer algo semelhante a:

```text
java version "1.8.0_431"
```

---

## Como compilar e executar:

1. Acesse, pelo terminal, a pasta onde está o arquivo principal do projeto.

   (A pasta contém um arquivo `Principal.java`.)

2. Compile o código:

```bash
javac Principal.java
```

3. Execute o programa:

```bash
java Principal
```
  
