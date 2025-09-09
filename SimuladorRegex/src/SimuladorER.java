import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

public class SimuladorER {

    // Remove espaços e padroniza união
    public static String padronizarER(String er) {

        // Remove todos os espaços da expressão
        String regex = er.replace(" ", "");

        // Substitui "+" por "|"
        regex = regex.replace("+", "|");

        // Remove as ocorrências do símbolo "ε"
        regex = regex.replace("ε", "");
        return regex;
    }

    public static int verificarParenteses(String er){
        if(er.charAt(0)==')') {
            System.out.println("Expressão inválida: iniciada com ')'");
            return 1;
        }
        if(er.charAt(er.length()-1)=='('){
            System.out.println("Expressão inválida: finalizada com '('");
            return 1;
        }
        char c;
        int cont=0;
        for(int i=0;i< er.length();i++){
            c = er.charAt(i);
            if(c=='(')
                cont++;
            else {
                if (c == ')')
                    cont--;
            }
        }
        if(cont<0 || cont>0) {
            System.out.println("Expressão inválida: aberturas e fechamentos '()' incoerentes.\nTente novamente.\n");
            return 1;
        }
        return 0;
    }

    public static int verificarERGeral(String er){
        char c;
        boolean flag=true;
        for(int i=0;i< er.length();i++){
            c = er.charAt(i);
            if(c=='(' && i+1<er.length()) {
                if (!Character.isLetter(er.charAt(i + 1)) && er.charAt(i+1)!='(') {
                    flag = false;
                    System.out.println("Expressão inválida: condição dentro de parenteses inválida.\nTente novamente.\n");
                }
            }
            else if(c=='|'){
                if(i-1>=0 && !Character.isLetter(er.charAt(i-1))) {
                    flag = false;
                    System.out.println("Expressão inválida: Nao há condição anterior a '|'.\nTente novamente.\n");
                }
                if(i+1<er.length() && !Character.isLetter(er.charAt(i+1))) {
                    flag = false;
                    System.out.println("Expressão inválida: nao há condição posterior a '|'.\nTente novamente.\n");
                }
            }
        }
        if(flag==false)
            return 1;
        return 0;
    }

    // Divide a expressão em tokens separados por concatenação '.'
    // Exemplo: (a|b)*.(a|b)* -> ["(a|b)*", "(a|b)*"]
    public static ArrayList<String> dividirTokens(String er) {
        ArrayList<String> tokens = new ArrayList<>();
        int nivelParenteses = 0;
        StringBuilder tokenAtual = new StringBuilder();

        for (int i = 0; i < er.length(); i++) {
            char c = er.charAt(i);

            if (c == '(') {
                nivelParenteses++;
                tokenAtual.append(c);
            } else if (c == ')') {
                nivelParenteses--;
                tokenAtual.append(c);
            } else if (c == '.' && nivelParenteses == 0) {
                // ponto de concatenação fora de parênteses: token completo
                tokens.add(tokenAtual.toString());
                tokenAtual.setLength(0);
            } else {
                tokenAtual.append(c);
            }
        }
        // adiciona último token
        if (tokenAtual.length() > 0) {
            tokens.add(tokenAtual.toString());
        }
        return tokens;
    }

    // Conta repetições consecutivas e gera forma abreviada com {n}
    // Exemplo: ["(a|b)", "(a|b)", "(a|b)*", "(a|b)*"] -> (a|b){2}(a|b)*{2}
    public static String gerarFormaAbreviada(ArrayList<String> tokens) {
        if (tokens.isEmpty()) return "";

        StringBuilder resultado = new StringBuilder();

        String atual = tokens.get(0);
        int contador = 1;

        for (int i = 1; i < tokens.size(); i++) {
            String proximo = tokens.get(i);
            if (proximo.equals(atual)) {
                contador++;
            } else {
                // adiciona token atual com contador
                resultado.append(atual);
                if (contador > 1) {
                    resultado.append("{").append(contador).append("}");
                }
                atual = proximo;
                contador = 1;
            }
        }
        // adiciona último token
        resultado.append(atual);
        if (contador > 1) {
            resultado.append("{").append(contador).append("}");
        }

        // adiciona ^ e $ para regex completa
        return "^" + resultado.toString() + "$";
    }

    // Converte ER do formato do anexo para regex Java (remove concatenação '.')
    public static String converterER(String er) {
        return padronizarER(er).replace(".", "");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        //int erVerificada;
        String erPadronizada, erEntrada;

       do{
            System.out.println("Digite a expressão regular (ex: (a|b).(a|b).(a|b)):");
            erEntrada = scanner.nextLine();
            erPadronizada = padronizarER(erEntrada);

        }while(verificarParenteses(erPadronizada)==1);

       do{
           System.out.println("Digite a expressão regular (ex: (a|b).(a|b).(a|b)):");
           erEntrada = scanner.nextLine();
           erPadronizada = padronizarER(erEntrada);

       }while(verificarERGeral(erPadronizada) == 1);

                ArrayList<String> tokens = dividirTokens(erPadronizada);

                System.out.println("Tokens extraídos:");
                for (String t : tokens) {
                    System.out.println(t);
                }

                String formaAbreviada = gerarFormaAbreviada(tokens);
                System.out.println("Forma abreviada gerada: " + formaAbreviada);

                String regexJava = converterER(erEntrada);
                System.out.println("Expressão convertida para regex Java: " + regexJava);

                Pattern pattern;
                try {
                    pattern = Pattern.compile(formaAbreviada);
                } catch (Exception e) {
                    System.out.println("Erro ao compilar a expressão regular: " + e.getMessage());
                    scanner.close();
                    return;
                }

                while (true) {
                    System.out.println("\nDigite uma palavra para testar (ou 'sair' para encerrar):");
                    String palavra = scanner.nextLine();
                    if (palavra.equalsIgnoreCase("sair")) {
                        break;
                    }

                    Matcher matcher = pattern.matcher(palavra);
                    if (matcher.matches()) {
                        System.out.println("A palavra '" + palavra + "' é aceita pela expressão.");
                    } else {
                        System.out.println("A palavra '" + palavra + "' NÃO é aceita pela expressão.");
                    }
                }

                scanner.close();
                System.out.println("Simulador encerrado.");

    }
}