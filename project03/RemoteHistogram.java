import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interfejs zdalnego serwisu tworzacego histogramy.
 */
public interface RemoteHistogram extends Remote {
	/**
	 * Utworzenie histogramu o okreslonej liczbie kubelkow.
	 * 
	 * @param bins liczba kubelkow
	 * @return unikalny numer identyfikujacy histogram
	 * @throws RemoteException
	 */
	public int createHistogram(int bins) throws RemoteException;

	/**
	 * Dodanie danej do histogramu o wskazanym identyfikatorze.
	 * 
	 * @param histogramID identyfikator histogramu, do ktorego nalezy dodac value
	 * @param value       wartosc do dodania do histogramu histogramID
	 * @throws RemoteException
	 */
	public void addToHistogram(int histogramID, int value) throws RemoteException;

	/**
	 * Pobranie histogramu o wskazanym identyfikatorze.
	 * 
	 * @param histogramID identyfikator histogramu
	 * @return tablica o rozmiarze bins z histogramem
	 * @throws RemoteException
	 */
	public int[] getHistogram(int histogramID) throws RemoteException;

}
