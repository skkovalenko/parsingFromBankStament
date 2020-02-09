/*
Написать код парсинга банковской выписки (файл movementsList.csv).
Код должен выводить сводную информацию по этой выписке:
Общий приход,
Общий расход,
Разбивка расходов.
*/

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.javamoney.moneta.Money;


import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.*;

public class Main {

    private static String movementListLink = "data/movementList.csv";
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");
    private static ArrayList<Movement> movementList = new ArrayList<>();

    private static CurrencyUnit rur = Monetary.getCurrency("RUR");
    
    private static final int INDEX_DATE = 3;
    private static final int INDEX_OPERATION_DESCRIPTION = 5;
    private static final int INDEX_AMOUNT_INCOME = 6;
    private static final int INDEX_AMOUNT_EXPENSE = 7;

    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static final Marker MARKER_EXCEPTIONS = MarkerManager.getMarker("EXCEPTIONS");

    public static void main(String[] args) {

        TreeMap<String, MonetaryAmount> sortExpenses = new TreeMap<>();
        System.out.println("Parsing movementList.csv:");

        try {
            loadInformationFromFile(movementListLink);
        } catch (IOException e) {
            LOGGER.error(MARKER_EXCEPTIONS, e.getMessage(), e);
            e.printStackTrace();
        }

        movementList.stream()
                .filter(movement -> movement.getTypeOfTransaction().equals(TypeOfTransaction.EXPENSE))
                .forEach(movement -> {
                    if(!sortExpenses.containsKey(movement.getCounterparty())){
                        sortExpenses.put(movement.getCounterparty(), Money.of(0, rur));
                    }
                    sortExpenses.put(movement.getCounterparty(), 
                            sortExpenses.get(movement.getCounterparty()).add(movement.getAmount()));
                });

        System.out.printf("\nIncome: %s\nExpense: %s\n\n",
                sumByTypeTransaction(TypeOfTransaction.INCOME), sumByTypeTransaction(TypeOfTransaction.EXPENSE));

        sortExpenses.forEach((s, monetaryAmount) -> System.out.printf("%s: %s\n", s, monetaryAmount));
    }

    private static MonetaryAmount sumByTypeTransaction(TypeOfTransaction typeTransaction){
            return movementList.stream()
                    .filter(movement -> movement.getTypeOfTransaction().equals(typeTransaction))
                    .map(Movement::getAmount)
                    .reduce(MonetaryAmount::add).orElse(Money.of(0.0, rur));
    }

    private static void loadInformationFromFile(String pathForMovementList) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(pathForMovementList));
        lines.stream().filter(s -> !s.intern()
                .equals(lines.get(0).intern()))
                .map(Main::parsingStringWithQuotes)
                .map(s -> s.split(","))
                .forEach(strings -> {
                    if(Double.parseDouble(strings[INDEX_AMOUNT_EXPENSE]) == 0){
                        movementList.add(new Movement(Money.of(Double.parseDouble(strings[INDEX_AMOUNT_INCOME]), rur),
                                TypeOfTransaction.INCOME,
                                parsingCounterParty(strings[INDEX_OPERATION_DESCRIPTION]),
                                parsingMCC(strings[INDEX_OPERATION_DESCRIPTION]),
                                LocalDate.parse(strings[INDEX_DATE], dateTimeFormatter)));
                    }else {
                        movementList.add(new Movement(Money.of(Double.parseDouble(strings[INDEX_AMOUNT_EXPENSE]), rur),
                                TypeOfTransaction.EXPENSE, parsingCounterParty(strings[INDEX_OPERATION_DESCRIPTION]),
                                parsingMCC(strings[INDEX_OPERATION_DESCRIPTION]),
                                LocalDate.parse(strings[INDEX_DATE], dateTimeFormatter)));
                    }
                });
    }

    private static String parsingCounterParty(String string){
        return stream(string.split("\\s{4,}"))
                .limit(2)
                .skip(1)
                .map(s -> s.replaceAll("^.+\\s*[\\\\/].+[\\\\/]", "").trim())
                .collect(Collectors.joining());
    }
    private static int parsingMCC(String string){
        return Integer.parseInt(string.substring(string.length() - 4));
    }

   private static String parsingStringWithQuotes(String stringForEditing){
        int l = stringForEditing.length();
        for(int i = 0; i < stringForEditing.length(); i++){
            if(String.valueOf(stringForEditing.charAt(i)).equals("\"")){
                String string1 = stringForEditing.substring(0, i);
                String string2 = stringForEditing.substring(i, l).replace("\"", "").replace(",",".");
                stringForEditing = string1.concat(string2);
            }
        }
        return stringForEditing;
    }
}
