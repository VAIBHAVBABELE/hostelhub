package hostelhub;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static hostelhub.Models.*;

/** Coordinates hostel operations and enforces basic business rules. */
public class HostelManager {
    private final Scanner scanner;
    private final DataStore.State data;

    public HostelManager(Scanner scanner) {
        this.scanner = scanner;
        this.data = DataStore.load();
    }

    public void save() { DataStore.save(data); }

    // ---------- Reusable input helpers ----------
    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            try { return Integer.parseInt(s); }
            catch (NumberFormatException ex) { System.out.println("Please enter a whole number."); }
        }
    }

    private String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("This field cannot be empty.");
        }
    }

    private String readOptional(String prompt, String current) {
        System.out.print(prompt + " [current: " + current + "]: ");
        String s = scanner.nextLine().trim();
        return s.isEmpty() ? current : s;
    }

    private BigDecimal readMoney(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                BigDecimal value = new BigDecimal(scanner.nextLine().trim());
                if (value.signum() >= 0) return value.setScale(2, java.math.RoundingMode.HALF_UP);
            } catch (NumberFormatException ex) { /* show below */ }
            System.out.println("Enter a valid amount greater than or equal to 0.");
        }
    }

    private boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String s = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
            if (s.equals("y") || s.equals("yes")) return true;
            if (s.equals("n") || s.equals("no")) return false;
            System.out.println("Please enter y or n.");
        }
    }

    private LocalDate readDate(String prompt, LocalDate defaultDate) {
        while (true) {
            System.out.print(prompt + " [YYYY-MM-DD, blank for " + defaultDate + "]: ");
            String s = scanner.nextLine().trim();
            if (s.isEmpty()) return defaultDate;
            try { return LocalDate.parse(s); }
            catch (DateTimeParseException ex) { System.out.println("Invalid date. Example: 2026-10-10"); }
        }
    }

    private <E extends Enum<E>> E readEnum(String prompt, Class<E> type) {
        E[] values = type.getEnumConstants();
        while (true) {
            System.out.println(prompt);
            for (int i = 0; i < values.length; i++) System.out.println((i + 1) + ". " + values[i]);
            int c = readInt("Choose: ");
            if (c >= 1 && c <= values.length) return values[c - 1];
            System.out.println("Invalid selection.");
        }
    }

    private String readEnumOptional(String prompt, String current, String[] options) {
        System.out.println(prompt + " (current: " + current + ")");
        for (int i = 0; i < options.length; i++) System.out.println((i + 1) + ". " + options[i]);
        System.out.println("0. Keep current");
        int c = readInt("Choose: ");
        return c >= 1 && c <= options.length ? options[c - 1] : current;
    }

    private Hostel findHostel(int id) { return data.hostels.get(id); }
    private Manager findManager(int id) { return data.managers.get(id); }
    private Tenant findTenant(int id) { return data.tenants.get(id); }
    private Room findRoom(int id) {
        for (Hostel h : data.hostels.values()) for (Room r : h.rooms) if (r.id == id) return r;
        return null;
    }
    private Facility findFacility(int hostelId, int facilityId) {
        Hostel h = findHostel(hostelId);
        if (h != null) for (Facility f : h.facilities) if (f.id == facilityId) return f;
        return null;
    }
    private List<Hostel> activeHostels() {
        return data.hostels.values().stream().filter(h -> h.active).collect(Collectors.toList());
    }
    private List<Tenant> activeTenants() {
        return data.tenants.values().stream().filter(t -> t.active).collect(Collectors.toList());
    }
    private List<Room> allRooms() {
        List<Room> result = new ArrayList<Room>();
        for (Hostel h : data.hostels.values()) result.addAll(h.rooms);
        return result;
    }
    private String hostelName(int id) {
        Hostel h = findHostel(id); return h == null ? "Unknown hostel" : h.name;
    }
    private String tenantName(int id) {
        Tenant t = findTenant(id); return t == null ? "Unknown tenant" : t.name;
    }
    private int occupiedBeds(Room room) {
        int count = 0;
        for (Tenant t : activeTenants()) if (t.roomId == room.id) count++;
        for (Booking b : data.bookings.values()) if (b.roomId == room.id && b.status == BookingStatus.BOOKED) count++;
        return count;
    }
    private boolean hasActiveTenants(Hostel h) {
        for (Tenant t : activeTenants()) if (t.hostelId == h.id) return true;
        return false;
    }
    private boolean isRoomNumberUsed(Hostel h, String number, int exceptRoomId) {
        for (Room r : h.rooms) if (r.active && r.id != exceptRoomId && r.number.equalsIgnoreCase(number)) return true;
        return false;
    }
    private Hostel chooseHostel(boolean activeOnly) {
        List<Hostel> list = activeOnly ? activeHostels() : new ArrayList<Hostel>(data.hostels.values());
        if (list.isEmpty()) { System.out.println("No hostels found. Add a hostel first."); return null; }
        for (Hostel h : list) System.out.println(h.id + ". " + h.name + " | " + h.type + " | " + h.address + (h.active ? "" : " [INACTIVE]"));
        int id = readInt("Enter hostel ID: ");
        Hostel h = findHostel(id);
        if (h == null || (activeOnly && !h.active)) { System.out.println("Hostel not found or inactive."); return null; }
        return h;
    }
    private Room chooseRoom(Hostel h, boolean onlyAvailable) {
        List<Room> list = h.rooms.stream().filter(r -> r.active && (!onlyAvailable || occupiedBeds(r) < r.capacity)).collect(Collectors.toList());
        if (list.isEmpty()) { System.out.println(onlyAvailable ? "No vacant rooms/beds available." : "No active rooms found."); return null; }
        for (Room r : list) printRoom(r);
        int id = readInt("Enter room ID: ");
        for (Room r : list) if (r.id == id) return r;
        System.out.println("Room not found in this hostel."); return null;
    }
    private void printRoom(Room r) {
        System.out.println("Room ID " + r.id + " | " + r.number + " | " + r.type + " | Rent ₹" + r.monthlyRent
                + " | Beds " + occupiedBeds(r) + "/" + r.capacity + " occupied | AC: " + (r.ac ? "Yes" : "No")
                + (r.active ? "" : " | INACTIVE"));
    }
    private void printHostel(Hostel h) {
        Manager m = findManager(h.managerId);
        int rooms = 0, capacity = 0, occupied = 0;
        for (Room r : h.rooms) if (r.active) { rooms++; capacity += r.capacity; occupied += occupiedBeds(r); }
        System.out.println("ID: " + h.id + " | " + h.name + " | " + h.type + " | " + h.address
                + " | Rent from ₹" + h.baseRent + " | Rooms: " + rooms + " | Vacant beds: " + Math.max(0, capacity - occupied)
                + " | Manager: " + (m == null ? "Unassigned" : m.name) + (h.active ? "" : " | INACTIVE"));
    }

    // ---------- Dashboard and reports ----------
    public void showDashboard() {
        int rooms = 0, capacity = 0, occupied = 0;
        for (Room r : allRooms()) if (r.active) { rooms++; capacity += r.capacity; occupied += occupiedBeds(r); }
        YearMonth month = YearMonth.now();
        BigDecimal collected = BigDecimal.ZERO;
        for (Payment p : data.payments.values()) if ((p.status == PaymentStatus.PAID || p.status == PaymentStatus.PARTIAL) && YearMonth.from(p.date).equals(month)) collected = collected.add(p.amount);
        BigDecimal outstanding = totalOutstanding();
        System.out.println("\n========== DASHBOARD ==========");
        System.out.println("Active hostels: " + activeHostels().size());
        System.out.println("Active managers: " + data.managers.values().stream().filter(m -> m.active).count());
        System.out.println("Active tenants: " + activeTenants().size());
        System.out.println("Active rooms: " + rooms);
        System.out.println("Vacant beds: " + Math.max(0, capacity - occupied));
        System.out.println("Occupancy: " + (capacity == 0 ? "0.0" : String.format(Locale.ROOT, "%.1f", occupied * 100.0 / capacity)) + "%");
        System.out.println("Payments received this month (" + month + "): ₹" + collected);
        System.out.println("Estimated outstanding balance: ₹" + outstanding);
        long open = data.complaints.values().stream().filter(c -> c.status == ComplaintStatus.OPEN || c.status == ComplaintStatus.IN_PROGRESS).count();
        System.out.println("Open/in-progress complaints: " + open);
    }

    // ---------- Hostel CRUD and search ----------
    public void addHostel() {
        String name = readRequired("Hostel name: ");
        String address = readRequired("Address/location: ");
        String phone = readRequired("Contact phone: ");
        HostelType type = readEnum("Hostel type:", HostelType.class);
        BigDecimal rent = readMoney("Starting monthly rent (₹): ");
        String description = readRequired("Short description/house rules: ");
        int managerId = chooseManagerId(true);
        Hostel h = new Hostel(data.nextHostelId++, name, address, phone, type, managerId, rent, description);
        data.hostels.put(h.id, h); save();
        System.out.println("Hostel added successfully. Hostel ID: " + h.id);
    }
    public void viewHostels() {
        if (data.hostels.isEmpty()) { System.out.println("No hostels registered."); return; }
        for (Hostel h : data.hostels.values()) printHostel(h);
    }
    public void searchHostels() {
        System.out.println("Filter by: 1. Name/location keyword  2. Type  3. Maximum starting rent  4. Available beds");
        int c = readInt("Choose filter: ");
        List<Hostel> result = activeHostels();
        if (c == 1) {
            String q = readRequired("Enter keyword: ").toLowerCase(Locale.ROOT);
            result = result.stream().filter(h -> h.name.toLowerCase(Locale.ROOT).contains(q) || h.address.toLowerCase(Locale.ROOT).contains(q)).collect(Collectors.toList());
        } else if (c == 2) {
            HostelType type = readEnum("Choose hostel type:", HostelType.class);
            result = result.stream().filter(h -> h.type == type).collect(Collectors.toList());
        } else if (c == 3) {
            BigDecimal max = readMoney("Maximum rent: ₹");
            result = result.stream().filter(h -> h.baseRent.compareTo(max) <= 0).collect(Collectors.toList());
        } else if (c == 4) {
            int min = Math.max(0, readInt("Minimum available beds: "));
            final int needed = min;
            result = result.stream().filter(h -> availableBeds(h) >= needed).collect(Collectors.toList());
        } else { System.out.println("Invalid filter."); return; }
        if (result.isEmpty()) System.out.println("No matching hostels found.");
        else for (Hostel h : result) printHostel(h);
    }
    private int availableBeds(Hostel h) {
        int n = 0; for (Room r : h.rooms) if (r.active) n += Math.max(0, r.capacity - occupiedBeds(r)); return n;
    }
    public void updateHostel() {
        Hostel h = chooseHostel(false); if (h == null) return;
        h.name = readOptional("Name", h.name); h.address = readOptional("Address", h.address);
        h.phone = readOptional("Phone", h.phone); h.description = readOptional("Description", h.description);
        h.baseRent = readMoney("New starting rent (₹): ");
        h.type = readEnum("Choose hostel type:", HostelType.class);
        if (readYesNo("Change assigned manager?")) h.managerId = chooseManagerId(true);
        save(); System.out.println("Hostel updated.");
    }
    public void deactivateHostel() {
        Hostel h = chooseHostel(true); if (h == null) return;
        if (hasActiveTenants(h)) { System.out.println("Cannot deactivate a hostel with active tenants. Check out tenants first."); return; }
        if (readYesNo("Deactivate " + h.name + "?")) { h.active = false; save(); System.out.println("Hostel deactivated."); }
    }
    public void viewHostelDetails() {
        Hostel h = chooseHostel(false); if (h == null) return;
        printHostel(h); System.out.println("Contact: " + h.phone); System.out.println("Description: " + h.description);
        System.out.println("Mess fee: ₹" + h.messMonthlyFee); System.out.println("Mess menu:"); printMenuList("Breakfast", h.breakfast); printMenuList("Lunch", h.lunch); printMenuList("Dinner", h.dinner);
        System.out.println("Facilities:"); if (h.facilities.isEmpty()) System.out.println("  None listed.");
        for (Facility f : h.facilities) System.out.println("  " + f.id + ". " + f.name + " | " + (f.available ? "Available" : "Unavailable") + " | ₹" + f.monthlyCharge + "/month");
        System.out.println("Rooms:"); for (Room r : h.rooms) if (r.active) printRoom(r);
    }

    // ---------- Manager CRUD ----------
    public void addManager() {
        String name = readRequired("Manager name: "); String phone = readRequired("Phone: ");
        String email = readRequired("Email: ");
        Manager m = new Manager(data.nextManagerId++, name, phone, email); data.managers.put(m.id, m); save();
        System.out.println("Manager registered. ID: " + m.id);
    }
    public void viewManagers() {
        if (data.managers.isEmpty()) { System.out.println("No managers registered."); return; }
        for (Manager m : data.managers.values()) {
            List<String> managed = data.hostels.values().stream().filter(h -> h.managerId == m.id && h.active).map(h -> h.name).collect(Collectors.toList());
            System.out.println("ID " + m.id + " | " + m.name + " | " + m.phone + " | " + m.email + " | Hostels: " + (managed.isEmpty() ? "None" : String.join(", ", managed)) + (m.active ? "" : " | INACTIVE"));
        }
    }
    public void searchManager() {
        String q = readRequired("Enter manager name/phone/email keyword: ").toLowerCase(Locale.ROOT);
        boolean found = false;
        for (Manager m : data.managers.values()) if (m.name.toLowerCase(Locale.ROOT).contains(q) || m.phone.toLowerCase(Locale.ROOT).contains(q) || m.email.toLowerCase(Locale.ROOT).contains(q)) {
            System.out.println("ID " + m.id + " | " + m.name + " | " + m.phone + " | " + m.email + (m.active ? "" : " | INACTIVE")); found = true;
        }
        if (!found) System.out.println("No manager matched.");
    }
    public void updateManager() {
        Manager m = chooseManager(false); if (m == null) return;
        m.name = readOptional("Name", m.name); m.phone = readOptional("Phone", m.phone); m.email = readOptional("Email", m.email); save();
        System.out.println("Manager updated.");
    }
    public void deactivateManager() {
        Manager m = chooseManager(true); if (m == null) return;
        boolean assigned = data.hostels.values().stream().anyMatch(h -> h.active && h.managerId == m.id);
        if (assigned) { System.out.println("Reassign this manager's active hostels before deactivation."); return; }
        m.active = false; save(); System.out.println("Manager deactivated.");
    }
    public void assignManager() {
        Hostel h = chooseHostel(true); if (h == null) return;
        int id = chooseManagerId(true); h.managerId = id; save(); System.out.println("Manager assigned to " + h.name + ".");
    }
    private Manager chooseManager(boolean activeOnly) {
        List<Manager> list = data.managers.values().stream().filter(m -> !activeOnly || m.active).collect(Collectors.toList());
        if (list.isEmpty()) { System.out.println("No managers found."); return null; }
        for (Manager m : list) System.out.println(m.id + ". " + m.name + " | " + m.phone + (m.active ? "" : " [INACTIVE]"));
        int id = readInt("Manager ID: "); Manager m = findManager(id);
        if (m == null || (activeOnly && !m.active)) { System.out.println("Manager not found."); return null; }
        return m;
    }
    private int chooseManagerId(boolean allowUnassigned) {
        List<Manager> list = data.managers.values().stream().filter(m -> m.active).collect(Collectors.toList());
        if (list.isEmpty()) { System.out.println("No active managers. Create a manager first; hostel will be unassigned."); return 0; }
        for (Manager m : list) System.out.println(m.id + ". " + m.name);
        if (allowUnassigned) System.out.println("0. Unassigned");
        int id = readInt("Manager ID: ");
        if (id == 0 && allowUnassigned) return 0;
        Manager m = findManager(id);
        if (m == null || !m.active) { System.out.println("Invalid manager; leaving unassigned."); return 0; }
        return id;
    }

    // ---------- Room CRUD ----------
    public void addRoom() {
        Hostel h = chooseHostel(true); if (h == null) return;
        String number = readRequired("Room number/name: ");
        if (isRoomNumberUsed(h, number, -1)) { System.out.println("That room number already exists in this hostel."); return; }
        RoomType type = readEnum("Room type:", RoomType.class);
        int defaultCapacity = type == RoomType.SINGLE ? 1 : type == RoomType.DOUBLE_SHARING ? 2 : type == RoomType.TRIPLE_SHARING ? 3 : 4;
        System.out.println("Suggested capacity for " + type + ": " + defaultCapacity);
        int capacity = readInt("Bed capacity (must be 1 or more): ");
        if (capacity < 1) { System.out.println("Capacity must be positive."); return; }
        BigDecimal rent = readMoney("Monthly rent for this room (₹): ");
        boolean ac = readYesNo("Does this room have AC?");
        Room r = new Room(data.nextRoomId++, h.id, number, type, capacity, rent, ac); h.rooms.add(r); save();
        System.out.println("Room added. Room ID: " + r.id);
    }
    public void viewRooms() {
        Hostel h = chooseHostel(false); if (h == null) return;
        if (h.rooms.isEmpty()) { System.out.println("No rooms in this hostel."); return; }
        for (Room r : h.rooms) printRoom(r);
    }
    public void searchRooms() {
        Hostel h = chooseHostel(true); if (h == null) return;
        System.out.println("1. By room number  2. By type  3. Maximum rent  4. Vacant only");
        int c = readInt("Choose filter: "); List<Room> result = h.rooms.stream().filter(r -> r.active).collect(Collectors.toList());
        if (c == 1) { String q = readRequired("Room number contains: ").toLowerCase(Locale.ROOT); result = result.stream().filter(r -> r.number.toLowerCase(Locale.ROOT).contains(q)).collect(Collectors.toList()); }
        else if (c == 2) { RoomType type = readEnum("Room type:", RoomType.class); result = result.stream().filter(r -> r.type == type).collect(Collectors.toList()); }
        else if (c == 3) { BigDecimal max = readMoney("Maximum rent: ₹"); result = result.stream().filter(r -> r.monthlyRent.compareTo(max) <= 0).collect(Collectors.toList()); }
        else if (c == 4) result = result.stream().filter(r -> occupiedBeds(r) < r.capacity).collect(Collectors.toList());
        else { System.out.println("Invalid filter."); return; }
        if (result.isEmpty()) System.out.println("No matching rooms."); else for (Room r : result) printRoom(r);
    }
    public void updateRoom() {
        Hostel h = chooseHostel(true); if (h == null) return; Room r = chooseRoom(h, false); if (r == null) return;
        String number = readOptional("Room number", r.number);
        if (isRoomNumberUsed(h, number, r.id)) { System.out.println("Room number already used."); return; }
        int capacity = readInt("New capacity [current " + r.capacity + "]: ");
        if (capacity < occupiedBeds(r)) { System.out.println("Capacity cannot be lower than current occupancy (" + occupiedBeds(r) + ")."); return; }
        if (capacity < 1) { System.out.println("Capacity must be positive."); return; }
        r.number = number; r.capacity = capacity; r.monthlyRent = readMoney("New monthly rent (₹): "); r.ac = readYesNo("Does this room have AC?"); save();
        System.out.println("Room updated.");
    }
    public void deactivateRoom() {
        Hostel h = chooseHostel(true); if (h == null) return; Room r = chooseRoom(h, false); if (r == null) return;
        if (occupiedBeds(r) > 0) { System.out.println("Cannot remove a room with occupants or confirmed bookings."); return; }
        r.active = false; save(); System.out.println("Room deactivated.");
    }
    public void viewVacancies() {
        Hostel h = chooseHostel(false); if (h == null) return;
        int vacantRooms = 0, vacantBeds = 0;
        for (Room r : h.rooms) if (r.active) { int n = Math.max(0, r.capacity - occupiedBeds(r)); vacantBeds += n; if (occupiedBeds(r) == 0) vacantRooms++; if (n > 0) printRoom(r); }
        System.out.println("Completely vacant rooms: " + vacantRooms + " | Vacant beds: " + vacantBeds);
    }

    // ---------- Tenant management and room allocation ----------
    public void registerTenant() {
        String name = readRequired("Tenant name: "); int age = readInt("Age: ");
        if (age < 18) { System.out.println("For this starter version, tenants must be at least 18."); return; }
        String phone = readRequired("Phone: "); String email = readRequired("Email: ");
        String emergency = readRequired("Emergency contact: "); Hostel h = chooseHostel(true); if (h == null) return;
        Room r = null;
        if (readYesNo("Allocate a room now?")) {
            r = chooseRoom(h, true); if (r == null) return;
            if (occupiedBeds(r) >= r.capacity) { System.out.println("Room is full."); return; }
        }
        LocalDate checkIn = readDate("Registration/check-in date", LocalDate.now()); String notes = readOptional("Notes/food preference", "None");
        Tenant t = new Tenant(data.nextTenantId++, name, age, phone, email, emergency, h.id, r == null ? 0 : r.id, checkIn, notes);
        data.tenants.put(t.id, t); save();
        System.out.println("Tenant registered. Tenant ID: " + t.id + (r == null ? ". No room assigned yet; use Check-in / allocate room." : ". Room allocated: " + r.number));
    }
    public void viewTenants() {
        List<Tenant> list = activeTenants(); if (list.isEmpty()) { System.out.println("No active tenants."); return; }
        for (Tenant t : list) printTenant(t);
    }
    private void printTenant(Tenant t) {
        Room r = findRoom(t.roomId);
        System.out.println("ID " + t.id + " | " + t.name + " | Age " + t.age + " | " + t.phone + " | " + hostelName(t.hostelId)
                + " | Room " + (r == null ? "Unassigned" : r.number) + " | Check-in " + t.checkInDate + (t.active ? "" : " | INACTIVE"));
    }
    public void searchTenant() {
        String q = readRequired("Search by name, phone, or tenant ID: ").toLowerCase(Locale.ROOT); boolean found = false;
        for (Tenant t : data.tenants.values()) if (String.valueOf(t.id).equals(q) || t.name.toLowerCase(Locale.ROOT).contains(q) || t.phone.toLowerCase(Locale.ROOT).contains(q)) { printTenant(t); found = true; }
        if (!found) System.out.println("No matching tenant.");
    }
    public void updateTenant() {
        Tenant t = chooseTenant(false); if (t == null) return;
        t.name = readOptional("Name", t.name); t.phone = readOptional("Phone", t.phone); t.email = readOptional("Email", t.email);
        t.emergencyContact = readOptional("Emergency contact", t.emergencyContact); t.notes = readOptional("Notes", t.notes); save();
        System.out.println("Tenant updated.");
    }
    public void checkInTenant() {
        Tenant t = chooseTenant(true); if (t == null) return;
        if (t.roomId != 0) { System.out.println("Tenant already has a room. Check out or update allocation first."); return; }
        Hostel h = findHostel(t.hostelId); if (h == null || !h.active) { h = chooseHostel(true); if (h == null) return; }
        Room r = chooseRoom(h, true); if (r == null) return;
        if (occupiedBeds(r) >= r.capacity) { System.out.println("Room is full."); return; }
        t.hostelId = h.id; t.roomId = r.id; t.checkInDate = readDate("Check-in date", LocalDate.now()); save();
        System.out.println("Tenant checked in to room " + r.number + ".");
    }
    public void checkOutTenant() {
        Tenant t = chooseTenant(true); if (t == null) return;
        if (totalTenantOutstanding(t.id).signum() > 0) System.out.println("Warning: estimated outstanding balance is ₹" + totalTenantOutstanding(t.id));
        if (!readYesNo("Confirm check-out for " + t.name + "?")) return;
        t.active = false; save(); System.out.println("Tenant checked out. Historical records retained.");
    }
    public void viewTenantsByHostelType() {
        HostelType type = readEnum("Hostel type:", HostelType.class); boolean found = false;
        for (Tenant t : activeTenants()) { Hostel h = findHostel(t.hostelId); if (h != null && h.type == type) { printTenant(t); found = true; } }
        if (!found) System.out.println("No active tenants found for " + type + ".");
    }
    public void viewTenantsByHostel() {
        Hostel h = chooseHostel(false); if (h == null) return; boolean found = false;
        for (Tenant t : activeTenants()) if (t.hostelId == h.id) { printTenant(t); found = true; }
        if (!found) System.out.println("No active tenants in this hostel.");
    }
    private Tenant chooseTenant(boolean activeOnly) {
        List<Tenant> list = data.tenants.values().stream().filter(t -> !activeOnly || t.active).collect(Collectors.toList());
        if (list.isEmpty()) { System.out.println("No tenants found."); return null; }
        for (Tenant t : list) printTenant(t);
        int id = readInt("Tenant ID: "); Tenant t = findTenant(id);
        if (t == null || (activeOnly && !t.active)) { System.out.println("Tenant not found."); return null; }
        return t;
    }

    // ---------- Mess menu ----------
    public void viewMessMenu() {
        Hostel h = chooseHostel(false); if (h == null) return;
        System.out.println("Mess at " + h.name + " | Monthly fee: ₹" + h.messMonthlyFee);
        printMenuList("Breakfast", h.breakfast); printMenuList("Lunch", h.lunch); printMenuList("Dinner", h.dinner);
    }
    private void printMenuList(String title, List<String> items) {
        System.out.println(title + ": " + (items.isEmpty() ? "Not set" : String.join(", ", items)));
    }
    public void updateMessMenu() {
        Hostel h = chooseHostel(true); if (h == null) return;
        System.out.println("1. Breakfast  2. Lunch  3. Dinner"); int c = readInt("Choose meal: ");
        List<String> target = c == 1 ? h.breakfast : c == 2 ? h.lunch : c == 3 ? h.dinner : null;
        if (target == null) { System.out.println("Invalid meal."); return; }
        System.out.println("Current: " + (target.isEmpty() ? "Not set" : String.join(", ", target)));
        String raw = readRequired("Enter comma-separated menu items: ");
        target.clear(); for (String item : raw.split(",")) if (!item.trim().isEmpty()) target.add(item.trim());
        save(); System.out.println("Menu updated.");
    }
    public void updateMessFee() {
        Hostel h = chooseHostel(true); if (h == null) return;
        h.messMonthlyFee = readMoney("New monthly mess fee (₹): "); save(); System.out.println("Mess fee updated.");
    }

    // ---------- Facilities CRUD ----------
    public void addFacility() {
        Hostel h = chooseHostel(true); if (h == null) return;
        String name = readRequired("Facility name (Wi-Fi, laundry, gym, etc.): ");
        String description = readRequired("Description: "); BigDecimal charge = readMoney("Monthly charge (₹, 0 if included): ");
        boolean available = readYesNo("Is it currently available?");
        Facility f = new Facility(data.nextFacilityId++, name, description, charge, available); h.facilities.add(f); save();
        System.out.println("Facility added. ID: " + f.id);
    }
    public void viewFacilities() {
        Hostel h = chooseHostel(false); if (h == null) return;
        if (h.facilities.isEmpty()) { System.out.println("No facilities listed."); return; }
        for (Facility f : h.facilities) System.out.println("ID " + f.id + " | " + f.name + " | " + f.description + " | ₹" + f.monthlyCharge + "/month | " + (f.available ? "Available" : "Unavailable"));
    }
    public void updateFacility() {
        Hostel h = chooseHostel(true); if (h == null) return; Facility f = chooseFacility(h); if (f == null) return;
        f.name = readOptional("Name", f.name); f.description = readOptional("Description", f.description);
        f.monthlyCharge = readMoney("New monthly charge (₹): "); f.available = readYesNo("Is it available?"); save(); System.out.println("Facility updated.");
    }
    public void removeFacility() {
        Hostel h = chooseHostel(true); if (h == null) return; Facility f = chooseFacility(h); if (f == null) return;
        h.facilities.remove(f); save(); System.out.println("Facility removed.");
    }
    private Facility chooseFacility(Hostel h) {
        if (h.facilities.isEmpty()) { System.out.println("No facilities listed."); return null; }
        for (Facility f : h.facilities) System.out.println(f.id + ". " + f.name + " | ₹" + f.monthlyCharge);
        int id = readInt("Facility ID: ");
        for (Facility f : h.facilities) if (f.id == id) return f;
        System.out.println("Facility not found."); return null;
    }

    // ---------- Payments, bills, and reports ----------
    public void recordPayment() {
        Tenant t = chooseTenant(true); if (t == null) return;
        BigDecimal amount = readMoney("Amount received (₹): "); if (amount.signum() == 0) { System.out.println("Payment must be greater than zero."); return; }
        PaymentType type = readEnum("Payment category:", PaymentType.class);
        PaymentStatus status = readEnum("Payment status:", PaymentStatus.class);
        if (status == PaymentStatus.VOID) { System.out.println("New payments cannot be recorded as VOID."); return; }
        LocalDate date = readDate("Payment date", LocalDate.now()); String note = readOptional("Reference/note", "None");
        Payment p = new Payment(data.nextPaymentId++, t.id, t.hostelId, amount, type, status, date, note);
        data.payments.put(p.id, p); save(); System.out.println("Payment recorded. Payment ID: " + p.id);
    }
    public void viewPayments() {
        if (data.payments.isEmpty()) { System.out.println("No payments recorded."); return; }
        for (Payment p : data.payments.values()) printPayment(p);
    }
    private void printPayment(Payment p) {
        System.out.println("Payment " + p.id + " | Tenant " + p.tenantId + " (" + tenantName(p.tenantId) + ") | " + p.type
                + " | ₹" + p.amount + " | " + p.status + " | " + p.date + " | " + p.note);
    }
    public void viewPendingBalances() {
        boolean found = false;
        for (Tenant t : activeTenants()) {
            BigDecimal balance = totalTenantOutstanding(t.id);
            if (balance.signum() > 0) { System.out.println("Tenant " + t.id + " | " + t.name + " | " + hostelName(t.hostelId) + " | Estimated balance: ₹" + balance); found = true; }
        }
        if (!found) System.out.println("No estimated outstanding balances. Note: balances are based on current monthly charges and recorded payments.");
    }
    public void monthlyCollectionReport() {
        int year = readInt("Year (e.g. 2026): "); int month = readInt("Month (1-12): ");
        if (month < 1 || month > 12 || year < 1900 || year > 9999) { System.out.println("Invalid year or month."); return; }
        YearMonth target = YearMonth.of(year, month); BigDecimal total = BigDecimal.ZERO;
        Map<Integer, BigDecimal> byHostel = new LinkedHashMap<Integer, BigDecimal>();
        for (Payment p : data.payments.values()) if (p.status != PaymentStatus.VOID && YearMonth.from(p.date).equals(target)) {
            total = total.add(p.amount); BigDecimal current = byHostel.containsKey(p.hostelId) ? byHostel.get(p.hostelId) : BigDecimal.ZERO;
            byHostel.put(p.hostelId, current.add(p.amount));
        }
        System.out.println("Collection report for " + target + ": ₹" + total);
        for (Map.Entry<Integer, BigDecimal> e : byHostel.entrySet()) System.out.println(hostelName(e.getKey()) + ": ₹" + e.getValue());
    }
    public void voidPayment() {
        viewPayments(); if (data.payments.isEmpty()) return;
        int id = readInt("Payment ID to void: ");
        Payment p = data.payments.get(id);
        if (p == null) { System.out.println("Payment not found."); return; }
        if (p.status == PaymentStatus.VOID) { System.out.println("Payment is already void."); return; }
        if (readYesNo("Void payment " + p.id + " for ₹" + p.amount + "?")) {
            p.status = PaymentStatus.VOID;
            save();
            System.out.println("Payment marked VOID. Original record retained for audit purposes.");
        }
    }

    public void tenantPaymentDetails() {
        Tenant t = chooseTenant(false); if (t == null) return;
        BigDecimal total = BigDecimal.ZERO; boolean found = false;
        for (Payment p : data.payments.values()) if (p.tenantId == t.id) { printPayment(p); if (p.status != PaymentStatus.VOID) total = total.add(p.amount); found = true; }
        if (!found) System.out.println("No payments recorded for this tenant.");
        System.out.println("Total recorded non-void payments: ₹" + total);
        System.out.println("Estimated current-month balance: ₹" + totalTenantOutstanding(t.id));
    }
    private BigDecimal totalOutstanding() {
        BigDecimal total = BigDecimal.ZERO; for (Tenant t : activeTenants()) total = total.add(totalTenantOutstanding(t.id)); return total;
    }
    private BigDecimal totalTenantOutstanding(int tenantId) {
        Tenant t = findTenant(tenantId); if (t == null || !t.active) return BigDecimal.ZERO;
        Room r = findRoom(t.roomId); Hostel h = findHostel(t.hostelId);
        if (r == null || h == null) return BigDecimal.ZERO;
        BigDecimal monthlyCharge = r.monthlyRent.add(h.messMonthlyFee);
        for (Facility f : h.facilities) if (f.available && f.monthlyCharge.signum() > 0) monthlyCharge = monthlyCharge.add(f.monthlyCharge);
        BigDecimal paid = BigDecimal.ZERO;
        YearMonth currentMonth = YearMonth.now();
        for (Payment p : data.payments.values()) if (p.tenantId == tenantId && (p.status == PaymentStatus.PAID || p.status == PaymentStatus.PARTIAL)
                && p.type != PaymentType.DEPOSIT && YearMonth.from(p.date).equals(currentMonth)) paid = paid.add(p.amount);
        BigDecimal balance = monthlyCharge.subtract(paid);
        return balance.signum() > 0 ? balance : BigDecimal.ZERO;
    }

    // ---------- Booking and complaint management ----------
    public void createBooking() {
        Hostel h = chooseHostel(true); if (h == null) return; Room r = chooseRoom(h, true); if (r == null) return;
        String name = readRequired("Applicant name: "); String phone = readRequired("Contact phone: ");
        LocalDate date = readDate("Expected move-in date", LocalDate.now().plusDays(7)); String notes = readOptional("Notes/preferences", "None");
        Booking b = new Booking(data.nextBookingId++, h.id, r.id, name, phone, date, BookingStatus.ENQUIRY, notes);
        data.bookings.put(b.id, b); save(); System.out.println("Booking enquiry created. ID: " + b.id);
    }
    public void viewBookings() {
        if (data.bookings.isEmpty()) { System.out.println("No bookings/enquiries."); return; }
        for (Booking b : data.bookings.values()) System.out.println("ID " + b.id + " | " + b.applicantName + " | " + hostelName(b.hostelId) + " | Room ID " + b.roomId + " | " + b.expectedDate + " | " + b.status + " | " + b.phone);
    }
    public void updateBookingStatus() {
        viewBookings(); if (data.bookings.isEmpty()) return;
        int id = readInt("Booking ID: "); Booking b = data.bookings.get(id); if (b == null) { System.out.println("Booking not found."); return; }
        BookingStatus status = readEnum("New booking status:", BookingStatus.class);
        if (status == BookingStatus.BOOKED) {
            Room r = findRoom(b.roomId);
            if (r == null || !r.active || occupiedBeds(r) >= r.capacity) { System.out.println("Room is not available; booking status unchanged."); return; }
        }
        b.status = status; save(); System.out.println("Booking status updated.");
    }
    public void createComplaint() {
        Hostel h = chooseHostel(true); if (h == null) return;
        int tenantId = 0;
        if (readYesNo("Is this complaint from a registered tenant?")) {
            Tenant t = chooseTenant(true); if (t == null) return;
            if (t.hostelId != h.id) { System.out.println("Selected tenant does not belong to this hostel."); return; }
            tenantId = t.id;
        }
        String title = readRequired("Complaint title: "); String details = readRequired("Complaint details: ");
        Complaint c = new Complaint(data.nextComplaintId++, h.id, tenantId, title, details, ComplaintStatus.OPEN, LocalDate.now());
        data.complaints.put(c.id, c); save(); System.out.println("Complaint registered. ID: " + c.id);
    }
    public void viewComplaints() {
        if (data.complaints.isEmpty()) { System.out.println("No complaints recorded."); return; }
        for (Complaint c : data.complaints.values()) System.out.println("ID " + c.id + " | " + hostelName(c.hostelId) + " | Tenant: " + (c.tenantId == 0 ? "Guest" : tenantName(c.tenantId)) + " | " + c.title + " | " + c.status + " | " + c.createdDate + " | " + c.details);
    }
    public void updateComplaintStatus() {
        viewComplaints(); if (data.complaints.isEmpty()) return;
        int id = readInt("Complaint ID: "); Complaint c = data.complaints.get(id); if (c == null) { System.out.println("Complaint not found."); return; }
        c.status = readEnum("New complaint status:", ComplaintStatus.class); save(); System.out.println("Complaint status updated.");
    }
}
