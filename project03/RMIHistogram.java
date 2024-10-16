import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class RMIHistogram extends UnicastRemoteObject implements RemoteHistogram, Binder {

    private final Map<Integer, int[]> histograms;
    private int nextId;

    public RMIHistogram() throws RemoteException {
        histograms = new HashMap<>();
        nextId = 1;
    }

    @Override
    public void bind(String serviceName) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            registry.rebind(serviceName, this);
            System.out.println("Service " + serviceName + " bound in registry");
        } catch (Exception e) {
            System.err.println("Error binding service " + serviceName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int createHistogram(int bins) throws RemoteException {
        synchronized (this) {
            int id = nextId++;
            histograms.put(id, new int[bins]);
            return id;
        }
    }

    @Override
    public void addToHistogram(int histogramId, int data) throws RemoteException {
        synchronized (this) {
            if (!histograms.containsKey(histogramId)) {
                throw new RemoteException("Histogram with ID " + histogramId + " does not exist.");
            }
            int[] histogram = histograms.get(histogramId);
            if (data < 0 || data >= histogram.length) {
                throw new RemoteException("Data value out of range.");
            }
            histogram[data]++;
        }
    }

    @Override
    public int[] getHistogram(int histogramId) throws RemoteException {
        synchronized (this) {
            if (!histograms.containsKey(histogramId)) {
                throw new RemoteException("Histogram with ID " + histogramId + " does not exist.");
            }
            return histograms.get(histogramId);
        }
    }
}