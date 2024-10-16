/**
 * Interfejs rejestracji uslugi RMI.
 */
public interface Binder {
	/**
	 * Rejestruje w rmiregistry pod podaną nazwa serwer usługi. Usluga rmiregistry
	 * bedzie uruchomiona pod adresem localhost:1099. Metoda nie może
	 * samodzielnie uruchamiać rmiregistry!
	 * 
	 * @param serviceName oczekiwana nazwa uslugi w rmiregistry
	 */
	public void bind(String serviceName);
}
