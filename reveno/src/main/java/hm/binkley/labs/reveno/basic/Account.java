package hm.binkley.labs.reveno.basic;

import lombok.RequiredArgsConstructor;

import java.util.Currency;

@RequiredArgsConstructor
public final class Account {
    public final String name;
    public final long balance;
    public final Currency currency;

    public Account(final String name, final long initialBalance) {
        this(name, initialBalance, Currency.getInstance("USD"));
    }

    public Account add(final long amount) {
        return new Account(name, balance + amount, currency);
    }
}
