package hm.binkley.labs;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static hm.binkley.labs.CompletableFutureMain.Images.ImageData.createDoneIcon;
import static hm.binkley.labs.CompletableFutureMain.Images.ImageData.createIcon;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

/**
 * {@code CompletableFutureMain} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 * @see <a href="http://www.infoq.com/articles/Functional-Style-Callbacks-Using-CompletableFuture">Functional-Style
 * Callbacks Using Java 8's CompletableFuture</a>
 */
public final class CompletableFutureMain {
    public static final class Images {
        private final ExecutorService executor = newSingleThreadExecutor();
        private final Function<ImageInfo, ImageData> dataFrom = info -> {
            final CompletableFuture<ImageData> data = supplyAsync(
                    info::downloadImage, executor);
            try {
                return data.get(5, SECONDS);
            } catch (InterruptedException e) {
                currentThread().interrupt();
                data.cancel(true);
                return createIcon(e);
            } catch (final ExecutionException e) {
                throw new RuntimeException(e.getCause());
            } catch (final TimeoutException e) {
                return createIcon(e);
            }
        };

        public static final class ImageData {
            public static ImageData createDoneIcon() {
                return null;
            }

            public static ImageData createIcon(final Exception e) {
                return null;
            }
        }

        public static final class ImageInfo {
            public static ImageData downloadImage(final ImageInfo info) {
                return null;
            }

            public ImageData downloadImage() {
                return new ImageData();
            }
        }

        public void renderPage(final CharSequence source) {
            CompletableFuture.allOf(findImages(source).stream().
                    map(info -> runAsync(
                            () -> render(dataFrom.apply(info)),
                            executor)).
                    toArray(CompletableFuture[]::new)).join();
            render(createDoneIcon());
        }

        private void render(final ImageData data) {

        }

        private List<ImageInfo> findImages(final CharSequence source) {
            return asList(new ImageInfo(), new ImageInfo());
        }
    }

    public static final class Trips {
        private final ExecutorService executor = newSingleThreadExecutor();

        public interface TripPlan {
            int getPrice();
        }

        public interface ServiceSupplier {
            TripPlan plan();
        }

        public TripPlan bestPlan(final Collection<ServiceSupplier> services) {
            return services.stream().
                    map(service -> supplyAsync(service::plan, executor)).
                    collect(toList()).stream().
                    min(comparing(plan -> plan.join().getPrice())).
                    get().
                    join();
        }
    }
}
