package hostelhub;

import java.io.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Stores application state between runs using Java object serialization. */
final class DataStore {
    private static final String FILE_NAME = "hostelhub-data.ser";
    private DataStore() {}

    static class State implements Serializable {
        private static final long serialVersionUID = 1L;
        Map<Integer, Models.Hostel> hostels = new LinkedHashMap<Integer, Models.Hostel>();
        Map<Integer, Models.Manager> managers = new LinkedHashMap<Integer, Models.Manager>();
        Map<Integer, Models.Tenant> tenants = new LinkedHashMap<Integer, Models.Tenant>();
        Map<Integer, Models.Payment> payments = new LinkedHashMap<Integer, Models.Payment>();
        Map<Integer, Models.Booking> bookings = new LinkedHashMap<Integer, Models.Booking>();
        Map<Integer, Models.Complaint> complaints = new LinkedHashMap<Integer, Models.Complaint>();
        int nextHostelId = 1, nextManagerId = 1, nextTenantId = 1, nextRoomId = 1;
        int nextFacilityId = 1, nextPaymentId = 1, nextBookingId = 1, nextComplaintId = 1;
    }

    static State load() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                Object obj = in.readObject();
                if (obj instanceof State) return (State) obj;
            } catch (IOException | ClassNotFoundException ex) {
                System.out.println("Saved data could not be loaded; starting with empty data. " + ex.getMessage());
            }
        }
        return new State();
    }

    static void save(State state) {
        File file = new File(FILE_NAME);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(state);
        } catch (IOException ex) {
            System.out.println("Warning: data could not be saved: " + ex.getMessage());
        }
    }
}
