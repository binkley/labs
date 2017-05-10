package hm.binkley.labs.reveno.basic;

import org.reveno.atp.api.Reveno;
import org.reveno.atp.api.commands.CommandContext;
import org.reveno.atp.api.transaction.TransactionContext;
import org.reveno.atp.core.Engine;
import org.slf4j.Logger;

import java.util.Currency;

import static org.slf4j.LoggerFactory.getLogger;

public final class RevenoMain {
    private static Reveno init(final String folder) {
        final Reveno reveno = new Engine(folder);
        reveno.domain().command(AddToBalanceCommand.class,
                AddToBalanceCommand::handler);
        reveno.domain().command(CreateAccount.class, Long.class,
                CreateAccount::handler);
        reveno.domain()
                .transactionAction(AddToBalance.class, AddToBalance::handler);
        reveno.domain().transactionAction(CreateAccount.class,
                CreateAccount::handler);
        reveno.domain().viewMapper(Account.class, AccountView.class,
                (id, e, r) -> new AccountView(id, e.name, e.balance));

        return reveno;
    }

    public static void main(final String... args) {
        Reveno reveno = init("/tmp/reveno");
        reveno.startup();

        final long id = reveno.executeSync(
                new CreateAccount("John", Currency.getInstance("EUR")));
        reveno.executeSync(new AddToBalanceCommand(id, 10000,
                Currency.getInstance("USD")));

        printStats(reveno, id);

        reveno.shutdown();
        reveno = init("/tmp/reveno");
        reveno.startup();

        printStats(reveno, id);

        reveno.shutdown();
    }

    private static void printStats(final Reveno reveno, final long id) {
        LOG.info("Balance of Account {}: {}", id,
                reveno.query().find(AccountView.class, id).balance);
    }

    /**
     * In current example it pays role of both Command and Transaction
     * Action.
     */
    private static class CreateAccount {
        public long id;
        public final String name;
        public final Currency currency;

        /*
         * Command handler.
         *
         * Much better to replace this with DSL -> see SimpleBankingAccountDSL
         */
        public static long handler(final CreateAccount cmd,
                final CommandContext ctx) {
            cmd.id = ctx.id(Account.class);
            ctx.executeTxAction(cmd);

            return cmd.id;
        }

        /*
         * Transaction Action handler.
         *
         * Much better to replace this with DSL -> see SimpleBankingAccountDSL
         */
        public static void handler(final CreateAccount tx,
                final TransactionContext ctx) {
            ctx.repo().store(tx.id, new Account(tx.name, 0, tx.currency));
        }

        public CreateAccount(final String name, final Currency currency) {
            this.name = name;
            this.currency = currency;
        }
    }

    public static class AddToBalanceCommand {
        public final long accountId;
        public final long amount;
        public final Currency currency;

        public static void handler(final AddToBalanceCommand cmd,
                final CommandContext ctx) {
            if (!ctx.repo().has(Account.class, cmd.accountId)) {
                throw new RuntimeException(
                        String.format("Account %s wasn't found!",
                                cmd.accountId));
            }
            final Account account = ctx.repo()
                    .get(Account.class, cmd.accountId);

            ctx.executeTxAction(new AddToBalance(cmd.accountId, converter
                    .convert(cmd.currency, account.currency, cmd.amount)));
        }

        public AddToBalanceCommand(final long accountId, final long amount,
                final Currency currency) {
            this.accountId = accountId;
            this.amount = amount;
            this.currency = currency;
        }

        protected static final CurrencyConverter converter
                = new DumbCurrencyConverter();
    }

    public static class AddToBalance {
        public final long accountId;
        public final long amount;

        public static void handler(final AddToBalance tx,
                final TransactionContext ctx) {
            ctx.repo().remap(tx.accountId, Account.class,
                    (id, a) -> a.add(tx.amount));
        }

        public AddToBalance(final long accountId, final long amount) {
            this.accountId = accountId;
            this.amount = amount;
        }
    }

    private static final Logger LOG = getLogger(RevenoMain.class);

    @FunctionalInterface
    interface CurrencyConverter {
        long convert(Currency from, Currency to, long amount);
    }

    private static class DumbCurrencyConverter
            implements CurrencyConverter {
        @Override
        public long convert(final Currency from, final Currency to,
                final long amount) {
            if ("USD".equals(from.getCurrencyCode()) && "EUR"
                    .equals(to.getCurrencyCode())) {
                return (long) (amount * 0.8822);
            }
            return amount;
        }
    }
}
