package hm.binkley.labs.reveno.basic;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BalanceChangedEvent {
    public final long accountId;
}
