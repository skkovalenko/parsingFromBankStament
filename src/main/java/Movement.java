import org.javamoney.moneta.Money;

import javax.money.MonetaryAmount;
import java.time.LocalDate;

public class Movement {

    private MonetaryAmount amount;
    private TypeOfTransaction typeOfTransaction;
    private String counterparty;
    private int MCC;
    private LocalDate date;

    public Movement(MonetaryAmount amount, TypeOfTransaction typeOfTransaction, String counterparty, int MCC, LocalDate date){
        this.amount = amount;
        this.typeOfTransaction = typeOfTransaction;
        this.counterparty = counterparty;
        this.MCC = MCC;
        this.date = date;
    }

    public MonetaryAmount getAmount() {
        return amount;
    }

    public TypeOfTransaction getTypeOfTransaction() {
        return typeOfTransaction;
    }

    public String getCounterparty() {
        return counterparty;
    }

    public int getMCC() {
        return MCC;
    }

    public LocalDate getDate() {
        return date;
    }
}
