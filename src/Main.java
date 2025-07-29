import java.io.IOException;
import java.util.Scanner;

public class Main {

    private final ConversionRateRequest conversionRateRequest = new ConversionRateRequest();
    private final Scanner scanner = new Scanner(System.in);

    public void showMenu() {
        System.out.println("--- Conversor de Monedas ---");
        System.out.println("Seleccione la moneda de origen:");
        System.out.println("1. USD (Dólar estadounidense)");
        System.out.println("2. EUR (Euro)");
        System.out.println("3. GBP (Libra esterlina)");
        System.out.println("4. JPY (Yen japonés)");
        System.out.println("5. CAD (Dólar canadiense)");
        System.out.println("6. AUD (Dólar australiano)");
        System.out.println("7. Salir");
        System.out.print("Ingrese su opción: ");
    }

    public int readUserOption() {
        while (!scanner.hasNextInt()) {
            System.out.println("Entrada inválida. Por favor, ingrese un número.");
            scanner.next(); // Limpiar el buffer
            System.out.print("Ingrese su opción: ");
        }
        return scanner.nextInt();
    }

    public String getCurrencyCode(int option) {
        return switch (option) {
            case 1 -> "USD";
            case 2 -> "EUR";
            case 3 -> "GBP";
            case 4 -> "JPY";
            case 5 -> "CAD";
            case 6 -> "AUD";
            default -> null; // Indica una opción inválida
        };
    }

    public String getTargetCurrency() {
        System.out.print("Ingrese la moneda de destino (ej: EUR): ");
        scanner.nextLine(); // Consumir la nueva línea pendiente del nextInt()
        return scanner.nextLine().trim().toUpperCase();
    }

    public double getAmount() {
        double n = 0.0;
        boolean validInput = false;
        while (!validInput) {
            System.out.print("Ingrese la cantidad a convertir: ");
            if (scanner.hasNextDouble()) {
                n = scanner.nextDouble();
                validInput = true;
            } else {
                System.out.println("Entrada inválida. Por favor, ingrese un número.");
                scanner.next(); // Limpiar el buffer
            }
        }
        scanner.nextLine(); // Consumir la nueva línea pendiente del nextDouble()
        return n;
    }

    public void showResult(double originalAmount, String sourceCurrency, double convertedAmount, String targetCurrency) {
        System.out.printf("%.2f %s equivalen a %.2f %s%n", originalAmount, sourceCurrency, convertedAmount, targetCurrency);
    }

    public void startConversion() {
        int option;
        do {
            showMenu();
            option = readUserOption();
            String sourceCurrency = getCurrencyCode(option);

            if (sourceCurrency != null) {
                String targetCurrency = getTargetCurrency();
                double amount = getAmount();

                try {
                    ConversionRate exchangeRate = conversionRateRequest.getConversionRate(sourceCurrency, targetCurrency);
                    double convertedAmount = amount * exchangeRate.amount();
                    showResult(amount, sourceCurrency, convertedAmount, exchangeRate.targetCurrency());
                } catch (IOException e) {
                    System.err.println("Error al obtener el tipo de cambio: " + e.getMessage());
                } catch (CurrencyNotFoundException e) {
                    System.err.println(e.getMessage());
                }
            } else if (option != 7) {
                System.out.println("Opción inválida. Por favor, intente de nuevo.");
            }
            System.out.println();
        } while (option != 7);

        System.out.println("¡Gracias por usar el conversor de monedas!");
        scanner.close();
    }

    public static void main(String[] args) {
        new Main().startConversion();
    }
}