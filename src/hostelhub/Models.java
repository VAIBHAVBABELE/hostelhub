package hostelhub;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Domain model classes for HostelHub. Kept together to make the starter project easy to navigate. */
final class Models {
    private Models() {}

    enum HostelType { BOYS, GIRLS, CO_LIVING }
    enum RoomType { SINGLE, DOUBLE_SHARING, TRIPLE_SHARING, FOUR_SHARING }
    enum PaymentType { RENT, MESS, DEPOSIT, FACILITY, OTHER }
    enum PaymentStatus { PAID, PARTIAL, PENDING, VOID }
    enum BookingStatus { ENQUIRY, BOOKED, CHECKED_IN, CANCELLED, CHECKED_OUT }
    enum ComplaintStatus { OPEN, IN_PROGRESS, RESOLVED, CLOSED }

    static class Hostel implements Serializable {
        private static final long serialVersionUID = 1L;
        int id;
        String name, address, phone, description;
        HostelType type;
        int managerId;
        BigDecimal baseRent;
        List<Room> rooms = new ArrayList<Room>();
        List<Facility> facilities = new ArrayList<Facility>();
        List<String> breakfast = new ArrayList<String>();
        List<String> lunch = new ArrayList<String>();
        List<String> dinner = new ArrayList<String>();
        BigDecimal messMonthlyFee = BigDecimal.ZERO;
        boolean active = true;

        Hostel(int id, String name, String address, String phone, HostelType type,
               int managerId, BigDecimal baseRent, String description) {
            this.id = id; this.name = name; this.address = address; this.phone = phone;
            this.type = type; this.managerId = managerId; this.baseRent = baseRent;
            this.description = description;
        }
    }

    static class Manager implements Serializable {
        private static final long serialVersionUID = 1L;
        int id;
        String name, phone, email;
        boolean active = true;
        Manager(int id, String name, String phone, String email) {
            this.id = id; this.name = name; this.phone = phone; this.email = email;
        }
    }

    static class Tenant implements Serializable {
        private static final long serialVersionUID = 1L;
        int id, hostelId, roomId;
        String name, phone, email, emergencyContact, notes;
        int age;
        LocalDate checkInDate;
        boolean active = true;
        Tenant(int id, String name, int age, String phone, String email,
               String emergencyContact, int hostelId, int roomId, LocalDate checkInDate, String notes) {
            this.id = id; this.name = name; this.age = age; this.phone = phone; this.email = email;
            this.emergencyContact = emergencyContact; this.hostelId = hostelId; this.roomId = roomId;
            this.checkInDate = checkInDate; this.notes = notes;
        }
    }

    static class Room implements Serializable {
        private static final long serialVersionUID = 1L;
        int id, hostelId, capacity;
        String number;
        RoomType type;
        BigDecimal monthlyRent;
        boolean ac, active = true;
        Room(int id, int hostelId, String number, RoomType type, int capacity, BigDecimal monthlyRent, boolean ac) {
            this.id = id; this.hostelId = hostelId; this.number = number; this.type = type;
            this.capacity = capacity; this.monthlyRent = monthlyRent; this.ac = ac;
        }
    }

    static class Facility implements Serializable {
        private static final long serialVersionUID = 1L;
        int id;
        String name, description;
        BigDecimal monthlyCharge;
        boolean available;
        Facility(int id, String name, String description, BigDecimal monthlyCharge, boolean available) {
            this.id = id; this.name = name; this.description = description;
            this.monthlyCharge = monthlyCharge; this.available = available;
        }
    }

    static class Payment implements Serializable {
        private static final long serialVersionUID = 1L;
        int id, tenantId, hostelId;
        BigDecimal amount;
        PaymentType type;
        PaymentStatus status;
        LocalDate date;
        String note;
        Payment(int id, int tenantId, int hostelId, BigDecimal amount, PaymentType type,
                PaymentStatus status, LocalDate date, String note) {
            this.id = id; this.tenantId = tenantId; this.hostelId = hostelId; this.amount = amount;
            this.type = type; this.status = status; this.date = date; this.note = note;
        }
    }

    static class Booking implements Serializable {
        private static final long serialVersionUID = 1L;
        int id, hostelId, roomId;
        String applicantName, phone, notes;
        LocalDate expectedDate;
        BookingStatus status;
        Booking(int id, int hostelId, int roomId, String applicantName, String phone,
                LocalDate expectedDate, BookingStatus status, String notes) {
            this.id = id; this.hostelId = hostelId; this.roomId = roomId; this.applicantName = applicantName;
            this.phone = phone; this.expectedDate = expectedDate; this.status = status; this.notes = notes;
        }
    }

    static class Complaint implements Serializable {
        private static final long serialVersionUID = 1L;
        int id, hostelId, tenantId;
        String title, details;
        ComplaintStatus status;
        LocalDate createdDate;
        Complaint(int id, int hostelId, int tenantId, String title, String details,
                  ComplaintStatus status, LocalDate createdDate) {
            this.id = id; this.hostelId = hostelId; this.tenantId = tenantId; this.title = title;
            this.details = details; this.status = status; this.createdDate = createdDate;
        }
    }
}
