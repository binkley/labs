package hm.binkley.labs.reveno.basic;

import org.reveno.atp.api.Reveno;
import org.reveno.atp.clustering.api.ClusterConfiguration;
import org.reveno.atp.clustering.api.InetAddress;
import org.reveno.atp.clustering.core.ClusterEngine;
import org.slf4j.Logger;

import static java.util.Collections.singletonList;
import static org.reveno.atp.utils.MapUtils.map;
import static org.slf4j.LoggerFactory.getLogger;

public final class RevenoDSLMain {
    private static ClusterEngine init(final String folder) {
        final ClusterEngine reveno = new ClusterEngine(folder);
        final InetAddress address = new InetAddress("127.0.0.1:16343",
                "yeth-marther");
        final ClusterConfiguration clusterConfiguration = reveno
                .clusterConfiguration();
        clusterConfiguration.currentNodeAddress(address);
        clusterConfiguration.nodesInetAddresses(singletonList(address));
        clusterConfiguration.dataSync().port(16343);

        reveno.events().eventHandler(BalanceChangedEvent.class, (e, m) -> {
            LOG.info("Replaying? {}", m.isRestore());
            final AccountView account = reveno.query()
                    .find(AccountView.class, e.accountId);
            LOG.info("New balance of account {} for {} from event is: {}",
                    e.accountId, account.name, account.balance);
        });

        reveno.domain().transaction("createAccount",
                (t, c) -> c.repo().store(t.id(), new Account(t.arg(), 0)))
                .uniqueIdFor(Account.class).command();
        reveno.domain().transaction("changeBalance", (t, c) -> {
            c.repo().store(t.longArg(), c.repo().get(Account.class, t.arg())
                    .add(t.intArg("inc")));
            c.eventBus().publishEvent(new BalanceChangedEvent(t.arg()));
        }).command();

        reveno.domain().viewMapper(Account.class, AccountView.class,
                (id, e, r) -> new AccountView(id, e.name, e.balance));

        return reveno;
    }

    private static void printStats(final Reveno reveno,
            final long accountId) {
        LOG.info("Account {} name: {}", accountId,
                reveno.query().find(AccountView.class, accountId).name);
        LOG.info("Account {} balance: {}", accountId,
                reveno.query().find(AccountView.class, accountId).balance);
    }

    public static void main(final String... args) {
        System.setProperty("java.net.preferIPv4Stack", "true");

        ClusterEngine reveno = init("/tmp/reveno");
        reveno.startup();

        final long accountId = reveno
                .executeSync("createAccount", map("name", "John"));
        reveno.executeSync("changeBalance",
                map("id", accountId, "inc", 10_000));

        printStats(reveno, accountId);
        reveno.shutdown();

        reveno = init("/tmp/reveno");
        reveno.startup();

        // we perform no operations here, just looking at last restored state
        printStats(reveno, accountId);

        reveno.shutdown();
    }

    private static final Logger LOG = getLogger(RevenoDSLMain.class);
}
