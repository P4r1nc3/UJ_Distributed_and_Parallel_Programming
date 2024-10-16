import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import java.util.Map;
import java.util.List;

public class ParallelEmployer implements Employer, ResultListener {

    // To set in the constructor
    private volatile Location exitLocation;
    private final Map<Location, Boolean> alreadyExploredLocations;
    private final Map<Integer, Location> orderIdToLocation;
    private final ExecutorService executorService;
    private final AtomicBoolean terminationSignal;
    private final Object lock;

    // To set in the setOrderInterface
    private OrderInterface orderInterface;

    public ParallelEmployer() {
        this.exitLocation = null;
        this.alreadyExploredLocations = new ConcurrentHashMap<>();
        this.orderIdToLocation = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        this.terminationSignal = new AtomicBoolean(false);
        this.lock = new Object();
    }

    @Override
    public void setOrderInterface(OrderInterface order) {
        this.orderInterface = order;
        order.setResultListener(this);
    }

    @Override
    public Location findExit(Location startLocation, List<Direction> allowedDirections) {
        exploreMaze(startLocation, allowedDirections);

        synchronized (lock) {
            while (checkCondition()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (executorService != null) {
            executorService.shutdown();
        }

        return exitLocation;
    }

    private boolean checkCondition() {
        return exitLocation == null && !Thread.currentThread().isInterrupted();
    }

    @Override
    public void result(Result result) {
        if (!terminationSignal.get()) {
            handleResultBasedOnType(result);
        }
    }

    private void handleResultBasedOnType(Result result) {
        switch (result.type()) {
            case EXIT -> handleExitResult(result);
            case PASSAGE -> handlePassageResult(result);
        }
    }

    private void handleExitResult(Result result) {
        exitLocation = orderIdToLocation.get(result.orderID());
        synchronized (lock) {
            terminationSignal.set(true);
            lock.notifyAll();
        }
    }

    private void handlePassageResult(Result result) {
        if (!executorService.isShutdown()) {
            executorService.submit(() ->
                    exploreMaze(orderIdToLocation.get(result.orderID()), result.allowedDirections()));
        }
    }

    private void exploreMaze(Location location, List<Direction> directions) {
        directions.stream()
                .map(possibleDirection -> possibleDirection.step(location))
                .forEach(this::exploreNewLocation);
    }

    private void exploreNewLocation(Location newLocation) {
        if (alreadyExploredLocations.putIfAbsent(newLocation, Boolean.TRUE) == null) {
            int orderId = orderInterface.order(newLocation);
            orderIdToLocation.put(orderId, newLocation);
        }
    }
}
