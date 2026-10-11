package hostelhub;

import java.util.Scanner;

/** Console entry point. All navigation uses nested menus and methods. */
public class HostelManagementSystem {
    private final Scanner scanner = new Scanner(System.in);
    private final HostelManager manager = new HostelManager(scanner);
    private boolean running = true;

    public static void main(String[] args) {
        new HostelManagementSystem().start();
    }

    private void start() {
        System.out.println("====================================");
        System.out.println(" Welcome to HostelHub Management");
        System.out.println("====================================");
        while (running) {
            showMainMenu();
            int choice = manager.readInt("Enter choice: ");
            switch (choice) {
                case 1: manager.showDashboard(); break;
                case 2: hostelMenu(); break;
                case 3: managerMenu(); break;
                case 4: tenantMenu(); break;
                case 5: roomMenu(); break;
                case 6: messMenu(); break;
                case 7: facilityMenu(); break;
                case 8: paymentMenu(); break;
                case 9: bookingComplaintMenu(); break;
                case 0: running = false; break;
                default: System.out.println("Invalid option. Please choose from the menu.");
            }
        }
        manager.save();
        scanner.close();
        System.out.println("HostelHub closed. Your data has been saved.");
    }

    private void showMainMenu() {
        System.out.println("\n========== HOSTELHUB ==========");
        System.out.println("1. Dashboard & Reports");
        System.out.println("2. Hostel Management");
        System.out.println("3. Manager Management");
        System.out.println("4. Tenant Management");
        System.out.println("5. Room Management");
        System.out.println("6. Mess Management");
        System.out.println("7. Facility Management");
        System.out.println("8. Payments & Billing");
        System.out.println("9. Bookings & Complaints");
        System.out.println("0. Save & Exit");
    }

    private void hostelMenu() {
        while (true) {
            submenuTitle("HOSTEL MANAGEMENT");
            System.out.println("1. Add hostel\n2. View all hostels\n3. Search/filter hostels\n4. Update hostel\n5. Deactivate hostel\n6. View hostel details\n0. Back");
            int c = manager.readInt("Enter choice: ");
            switch (c) {
                case 1: manager.addHostel(); break;
                case 2: manager.viewHostels(); break;
                case 3: manager.searchHostels(); break;
                case 4: manager.updateHostel(); break;
                case 5: manager.deactivateHostel(); break;
                case 6: manager.viewHostelDetails(); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void managerMenu() {
        while (true) {
            submenuTitle("MANAGER MANAGEMENT");
            System.out.println("1. Add manager\n2. View managers\n3. Search manager\n4. Update manager\n5. Deactivate manager\n6. Assign manager to hostel\n0. Back");
            int c = manager.readInt("Enter choice: ");
            switch (c) {
                case 1: manager.addManager(); break;
                case 2: manager.viewManagers(); break;
                case 3: manager.searchManager(); break;
                case 4: manager.updateManager(); break;
                case 5: manager.deactivateManager(); break;
                case 6: manager.assignManager(); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void tenantMenu() {
        while (true) {
            submenuTitle("TENANT MANAGEMENT");
            System.out.println("1. Register tenant\n2. View all active tenants\n3. Search tenant\n4. Update tenant\n5. Check-in / allocate room\n6. Check-out tenant\n7. View tenants by hostel type\n8. View tenants by hostel\n0. Back");
            int c = manager.readInt("Enter choice: ");
            switch (c) {
                case 1: manager.registerTenant(); break;
                case 2: manager.viewTenants(); break;
                case 3: manager.searchTenant(); break;
                case 4: manager.updateTenant(); break;
                case 5: manager.checkInTenant(); break;
                case 6: manager.checkOutTenant(); break;
                case 7: manager.viewTenantsByHostelType(); break;
                case 8: manager.viewTenantsByHostel(); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void roomMenu() {
        while (true) {
            submenuTitle("ROOM MANAGEMENT");
            System.out.println("1. Add room\n2. View rooms\n3. Search/filter rooms\n4. Update room\n5. Deactivate room\n6. View vacant rooms/beds\n0. Back");
            int c = manager.readInt("Enter choice: ");
            switch (c) {
                case 1: manager.addRoom(); break;
                case 2: manager.viewRooms(); break;
                case 3: manager.searchRooms(); break;
                case 4: manager.updateRoom(); break;
                case 5: manager.deactivateRoom(); break;
                case 6: manager.viewVacancies(); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void messMenu() {
        while (true) {
            submenuTitle("MESS MANAGEMENT");
            System.out.println("1. View mess menu\n2. Update meal menu\n3. Update monthly mess fee\n0. Back");
            int c = manager.readInt("Enter choice: ");
            switch (c) {
                case 1: manager.viewMessMenu(); break;
                case 2: manager.updateMessMenu(); break;
                case 3: manager.updateMessFee(); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void facilityMenu() {
        while (true) {
            submenuTitle("FACILITY MANAGEMENT");
            System.out.println("1. Add facility\n2. View facilities\n3. Update facility\n4. Remove facility\n0. Back");
            int c = manager.readInt("Enter choice: ");
            switch (c) {
                case 1: manager.addFacility(); break;
                case 2: manager.viewFacilities(); break;
                case 3: manager.updateFacility(); break;
                case 4: manager.removeFacility(); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void paymentMenu() {
        while (true) {
            submenuTitle("PAYMENTS & BILLING");
            System.out.println("1. Record payment\n2. View payment history\n3. View pending tenant balances\n4. Monthly collection report\n5. View tenant payment details\n6. Void a payment record\n0. Back");
            int c = manager.readInt("Enter choice: ");
            switch (c) {
                case 1: manager.recordPayment(); break;
                case 2: manager.viewPayments(); break;
                case 3: manager.viewPendingBalances(); break;
                case 4: manager.monthlyCollectionReport(); break;
                case 5: manager.tenantPaymentDetails(); break;
                case 6: manager.voidPayment(); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void bookingComplaintMenu() {
        while (true) {
            submenuTitle("BOOKINGS & COMPLAINTS");
            System.out.println("1. Create booking/enquiry\n2. View bookings\n3. Update booking status\n4. Register complaint\n5. View complaints\n6. Update complaint status\n0. Back");
            int c = manager.readInt("Enter choice: ");
            switch (c) {
                case 1: manager.createBooking(); break;
                case 2: manager.viewBookings(); break;
                case 3: manager.updateBookingStatus(); break;
                case 4: manager.createComplaint(); break;
                case 5: manager.viewComplaints(); break;
                case 6: manager.updateComplaintStatus(); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void submenuTitle(String title) {
        System.out.println("\n========== " + title + " ==========");
    }
}
