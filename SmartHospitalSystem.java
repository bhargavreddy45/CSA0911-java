import java.util.*;

class HospitalException extends Exception {
    public HospitalException(String message) {
        super(message);
    }
}

class InvalidPatientException extends HospitalException {
    public InvalidPatientException(String message) {
        super(message);
    }
}

class DuplicateAppointmentException extends HospitalException {
    public DuplicateAppointmentException(String message) {
        super(message);
    }
}

class OutOfStockException extends HospitalException {
    public OutOfStockException(String message) {
        super(message);
    }
}

class Patient {
    private int patientId;
    private String name;
    private int age;
    private String phone;

    public Patient(int patientId, String name, int age, String phone) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.phone = phone;
    }

    public int getPatientId() {
        return patientId;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getPhone() {
        return phone;
    }

    public String getPriority() {
        return "REGULAR";
    }

    public void display() {
        System.out.println(
                "ID: " + patientId +
                " | Name: " + name +
                " | Age: " + age +
                " | Phone: " + phone +
                " | Type: " + getPriority()
        );
    }
}

class EmergencyPatient extends Patient {

    public EmergencyPatient(int id, String name, int age, String phone) {
        super(id, name, age, phone);
    }

    @Override
    public String getPriority() {
        return "EMERGENCY";
    }
}

class Doctor {
    private int doctorId;
    private String name;
    private String specialization;
    private int maxSlots;
    private int bookedSlots;

    public Doctor(int doctorId, String name,
                  String specialization, int maxSlots) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialization = specialization;
        this.maxSlots = maxSlots;
        this.bookedSlots = 0;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public String getName() {
        return name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public int getAvailableSlots() {
        return maxSlots - bookedSlots;
    }

    public synchronized boolean bookSlot() {
        if (bookedSlots < maxSlots) {
            bookedSlots++;
            return true;
        }
        return false;
    }

    public synchronized void cancelSlot() {
        if (bookedSlots > 0) {
            bookedSlots--;
        }
    }

    public void display() {
        System.out.println(
                "Doctor ID: " + doctorId +
                " | Name: " + name +
                " | Specialization: " + specialization +
                " | Available Slots: " + getAvailableSlots()
        );
    }
}

class Appointment {
    private int appointmentId;
    private Patient patient;
    private Doctor doctor;
    private String date;
    private String status;

    public Appointment(int appointmentId,
                       Patient patient,
                       Doctor doctor,
                       String date) {
        this.appointmentId = appointmentId;
        this.patient = patient;
        this.doctor = doctor;
        this.date = date;
        this.status = "BOOKED";
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public String getStatus() {
        return status;
    }

    public void cancel() {
        status = "CANCELLED";
    }

    public void display() {
        System.out.println(
                "Appointment ID: " + appointmentId +
                " | Patient: " + patient.getName() +
                " | Doctor: " + doctor.getName() +
                " | Date: " + date +
                " | Status: " + status
        );
    }
}

class Medicine {
    private int medicineId;
    private String name;
    private int stock;
    private int minimumStock;

    public Medicine(int medicineId, String name,
                    int stock, int minimumStock) {
        this.medicineId = medicineId;
        this.name = name;
        this.stock = stock;
        this.minimumStock = minimumStock;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public String getName() {
        return name;
    }

    public synchronized int getStock() {
        return stock;
    }

    public synchronized void addStock(int quantity) {
        stock += quantity;
    }

    public synchronized void issueMedicine(int quantity)
            throws OutOfStockException {

        if (quantity > stock) {
            throw new OutOfStockException(
                    "Insufficient stock for " + name
            );
        }

        stock -= quantity;
    }

    public boolean isLowStock() {
        return stock <= minimumStock;
    }

    public void display() {
        System.out.println(
                "Medicine ID: " + medicineId +
                " | Name: " + name +
                " | Stock: " + stock +
                " | Minimum Level: " + minimumStock
        );
    }
}

class Notification {
    private int notificationId;
    private String message;

    public Notification(int notificationId, String message) {
        this.notificationId = notificationId;
        this.message = message;
    }

    public void send() {
        System.out.println(
                "[NOTIFICATION " + notificationId + "] "
                        + message
        );
    }
}

class AppointmentBookingThread extends Thread {

    private SmartHospitalSystem system;

    public AppointmentBookingThread(SmartHospitalSystem system) {
        this.system = system;
    }

    @Override
    public void run() {
        System.out.println(
                "\nAppointment Booking Thread Started..."
        );

        synchronized (system) {
            System.out.println(
                    "Checking appointment resources..."
            );

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Booking thread interrupted.");
            }
        }

        System.out.println(
                "Appointment Booking Thread Completed."
        );
    }
}

class NotificationThread extends Thread {

    private SmartHospitalSystem system;

    public NotificationThread(SmartHospitalSystem system) {
        this.system = system;
    }

    @Override
    public void run() {

        System.out.println(
                "\nNotification Thread Started..."
        );

        synchronized (system) {

            for (Medicine medicine :
                    system.getMedicines().values()) {

                if (medicine.isLowStock()) {

                    Notification notification =
                            new Notification(
                                    medicine.getMedicineId(),
                                    "LOW STOCK ALERT: "
                                            + medicine.getName()
                            );

                    notification.send();
                }
            }
        }

        System.out.println(
                "Notification Thread Completed."
        );
    }
}

public class SmartHospitalSystem {

    private ArrayList<Patient> patients =
            new ArrayList<>();

    private ArrayList<Doctor> doctors =
            new ArrayList<>();

    private ArrayList<Appointment> appointments =
            new ArrayList<>();

    private HashMap<Integer, Medicine> medicines =
            new HashMap<>();

    private Set<Integer> doctorIds =
            new HashSet<>();

    private Hashtable<Integer, Notification> notifications =
            new Hashtable<>();

    private Queue<Patient> waitlist =
            new LinkedList<>();

    private int appointmentCounter = 1001;

    public ArrayList<Patient> getPatients() {
        return patients;
    }

    public HashMap<Integer, Medicine> getMedicines() {
        return medicines;
    }

    public synchronized void registerPatient(
            Scanner sc) {

        System.out.print("Patient ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("Patient Name: ");
        String name = sc.nextLine();

        System.out.print("Age: ");
        int age = sc.nextInt();
        sc.nextLine();

        System.out.print("Phone: ");
        String phone = sc.nextLine();

        System.out.print(
                "Emergency Patient? (yes/no): "
        );

        String emergency = sc.nextLine();

        Patient patient;

        if (emergency.equalsIgnoreCase("yes")) {

            patient = new EmergencyPatient(
                    id, name, age, phone
            );

        } else {

            patient = new Patient(
                    id, name, age, phone
            );
        }

        patients.add(patient);

        System.out.println(
                "Patient registered successfully."
        );
    }

    public synchronized void registerDoctor(
            Scanner sc) {

        System.out.print("Doctor ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        if (doctorIds.contains(id)) {
            System.out.println(
                    "Doctor ID already exists."
            );
            return;
        }

        System.out.print("Doctor Name: ");
        String name = sc.nextLine();

        System.out.print("Specialization: ");
        String specialization = sc.nextLine();

        System.out.print("Maximum Slots: ");
        int slots = sc.nextInt();

        Doctor doctor = new Doctor(
                id, name, specialization, slots
        );

        doctors.add(doctor);
        doctorIds.add(id);

        System.out.println(
                "Doctor registered successfully."
        );
    }

    public synchronized void addMedicine(
            Scanner sc) {

        System.out.print("Medicine ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("Medicine Name: ");
        String name = sc.nextLine();

        System.out.print("Initial Stock: ");
        int stock = sc.nextInt();

        System.out.print("Minimum Stock Level: ");
        int minimum = sc.nextInt();

        medicines.put(
                id,
                new Medicine(
                        id, name, stock, minimum
                )
        );

        System.out.println(
                "Medicine added successfully."
        );
    }

    private Patient findPatient(int id)
            throws InvalidPatientException {

        for (Patient patient : patients) {

            if (patient.getPatientId() == id) {
                return patient;
            }
        }

        throw new InvalidPatientException(
                "Patient ID not found."
        );
    }

    private Doctor findDoctor(int id) {

        for (Doctor doctor : doctors) {

            if (doctor.getDoctorId() == id) {
                return doctor;
            }
        }

        return null;
    }

    public synchronized void bookAppointment(
            Scanner sc) {

        try {

            System.out.print("Patient ID: ");
            int patientId = sc.nextInt();

            System.out.print("Doctor ID: ");
            int doctorId = sc.nextInt();

            sc.nextLine();

            System.out.print("Appointment Date: ");
            String date = sc.nextLine();

            Patient patient =
                    findPatient(patientId);

            Doctor doctor =
                    findDoctor(doctorId);

            if (doctor == null) {
                System.out.println(
                        "Doctor not found."
                );
                return;
            }

            for (Appointment appointment :
                    appointments) {

                if (appointment.getPatient()
                        .getPatientId() == patientId
                        && appointment.getDoctor()
                        .getDoctorId() == doctorId
                        && appointment.getStatus()
                        .equals("BOOKED")) {

                    throw new DuplicateAppointmentException(
                            "Duplicate appointment detected."
                    );
                }
            }

            if (doctor.bookSlot()) {

                Appointment appointment =
                        new Appointment(
                                appointmentCounter++,
                                patient,
                                doctor,
                                date
                        );

                appointments.add(appointment);

                System.out.println(
                        "Appointment booked successfully."
                );

                appointment.display();

            } else {

                waitlist.add(patient);

                System.out.println(
                        "Doctor is fully booked."
                );

                System.out.println(
                        "Patient added to waitlist."
                );
            }

        } catch (HospitalException e) {

            System.out.println(
                    "ERROR: " + e.getMessage()
            );
        }
    }

    public synchronized void cancelAppointment(
            Scanner sc) {

        System.out.print(
                "Enter Appointment ID: "
        );

        int id = sc.nextInt();

        ListIterator<Appointment> iterator =
                appointments.listIterator();

        while (iterator.hasNext()) {

            Appointment appointment =
                    iterator.next();

            if (appointment.getAppointmentId() == id
                    && appointment.getStatus()
                    .equals("BOOKED")) {

                appointment.cancel();

                appointment.getDoctor()
                        .cancelSlot();

                System.out.println(
                        "Appointment cancelled."
                );

                if (!waitlist.isEmpty()) {

                    Patient next =
                            waitlist.poll();

                    System.out.println(
                            "Waitlisted patient promoted: "
                                    + next.getName()
                    );
                }

                return;
            }
        }

        System.out.println(
                "Appointment not found."
        );
    }

    public void searchPatient(Scanner sc) {

        System.out.print(
                "Enter Patient ID: "
        );

        int id = sc.nextInt();

        Iterator<Patient> iterator =
                patients.iterator();

        while (iterator.hasNext()) {

            Patient patient =
                    iterator.next();

            if (patient.getPatientId() == id) {

                patient.display();
                return;
            }
        }

        System.out.println(
                "Patient not found."
        );
    }

    public void searchMedicine(Scanner sc) {

        System.out.print(
                "Enter Medicine ID: "
        );

        int id = sc.nextInt();

        Medicine medicine =
                medicines.get(id);

        if (medicine != null) {

            medicine.display();

        } else {

            System.out.println(
                    "Medicine not found."
            );
        }
    }

    public synchronized void updateMedicineStock(
            Scanner sc) {

        System.out.print(
                "Enter Medicine ID: "
        );

        int id = sc.nextInt();

        Medicine medicine =
                medicines.get(id);

        if (medicine == null) {

            System.out.println(
                    "Medicine not found."
            );

            return;
        }

        System.out.print(
                "Enter quantity to add: "
        );

        int quantity = sc.nextInt();

        medicine.addStock(quantity);

        System.out.println(
                "Stock updated successfully."
        );

        medicine.display();
    }

    public synchronized void issueMedicine(
            Scanner sc) {

        System.out.print(
                "Enter Medicine ID: "
        );

        int id = sc.nextInt();

        Medicine medicine =
                medicines.get(id);

        if (medicine == null) {

            System.out.println(
                    "Medicine not found."
            );

            return;
        }

        System.out.print(
                "Enter quantity: "
        );

        int quantity = sc.nextInt();

        try {

            medicine.issueMedicine(quantity);

            System.out.println(
                    "Medicine issued successfully."
            );

            medicine.display();

        } catch (OutOfStockException e) {

            System.out.println(
                    "ERROR: " + e.getMessage()
            );
        }
    }

    public void displayWaitlist() {

        if (waitlist.isEmpty()) {

            System.out.println(
                    "Waitlist is empty."
            );

            return;
        }

        System.out.println(
                "\n--- WAITLIST ---"
        );

        for (Patient patient : waitlist) {
            patient.display();
        }
    }

    public void patientReport() {

        System.out.println(
                "\n===== PATIENT REPORT ====="
        );

        for (Patient patient : patients) {
            patient.display();
        }

        System.out.println(
                "\n===== APPOINTMENTS ====="
        );

        for (Appointment appointment :
                appointments) {

            appointment.display();
        }
    }

    public void pharmacyReport() {

        System.out.println(
                "\n===== PHARMACY INVENTORY REPORT ====="
        );

        for (Medicine medicine :
                medicines.values()) {

            medicine.display();

            if (medicine.isLowStock()) {

                System.out.println(
                        "STATUS: LOW STOCK"
                );

            } else {

                System.out.println(
                        "STATUS: AVAILABLE"
                );
            }
        }
    }

    public void startThreads() {

        AppointmentBookingThread bookingThread =
                new AppointmentBookingThread(this);

        NotificationThread notificationThread =
                new NotificationThread(this);

        bookingThread.setPriority(
                Thread.MAX_PRIORITY
        );

        notificationThread.setPriority(
                Thread.NORM_PRIORITY
        );

        bookingThread.start();
        notificationThread.start();

        try {

            bookingThread.join();
            notificationThread.join();

        } catch (InterruptedException e) {

            System.out.println(
                    "Thread execution interrupted."
            );
        }
    }

    public void displayDoctors() {

        System.out.println(
                "\n===== DOCTORS ====="
        );

        for (Doctor doctor : doctors) {
            doctor.display();
        }
    }

    public void menu() {

        Scanner sc = new Scanner(System.in);

        int choice;

        do {

            System.out.println(
                    "\n======================================"
            );

            System.out.println(
                    " SMART HOSPITAL MANAGEMENT SYSTEM"
            );

            System.out.println(
                    "======================================"
            );

            System.out.println(
                    "1. Register Patient"
            );

            System.out.println(
                    "2. Register Doctor"
            );

            System.out.println(
                    "3. Add Medicine"
            );

            System.out.println(
                    "4. Book Appointment"
            );

            System.out.println(
                    "5. Cancel Appointment"
            );

            System.out.println(
                    "6. Search Patient"
            );

            System.out.println(
                    "7. Search Medicine"
            );

            System.out.println(
                    "8. Update Medicine Stock"
            );

            System.out.println(
                    "9. Issue Medicine"
            );

            System.out.println(
                    "10. Display Waitlist"
            );

            System.out.println(
                    "11. Patient Report"
            );

            System.out.println(
                    "12. Pharmacy Report"
            );

            System.out.println(
                    "13. Display Doctors"
            );

            System.out.println(
                    "14. Start Notification & Booking Threads"
            );

            System.out.println(
                    "15. Exit"
            );

            System.out.print(
                    "\nEnter your choice: "
            );

            choice = sc.nextInt();

            try {

                switch (choice) {

                    case 1:
                        registerPatient(sc);
                        break;

                    case 2:
                        registerDoctor(sc);
                        break;

                    case 3:
                        addMedicine(sc);
                        break;

                    case 4:
                        bookAppointment(sc);
                        break;

                    case 5:
                        cancelAppointment(sc);
                        break;

                    case 6:
                        searchPatient(sc);
                        break;

                    case 7:
                        searchMedicine(sc);
                        break;

                    case 8:
                        updateMedicineStock(sc);
                        break;

                    case 9:
                        issueMedicine(sc);
                        break;

                    case 10:
                        displayWaitlist();
                        break;

                    case 11:
                        patientReport();
                        break;

                    case 12:
                        pharmacyReport();
                        break;

                    case 13:
                        displayDoctors();
                        break;

                    case 14:
                        startThreads();
                        break;

                    case 15:
                        System.out.println(
                                "Thank you for using Smart Hospital System."
                        );
                        break;

                    default:
                        System.out.println(
                                "Invalid menu choice."
                        );
                }

            } catch (InputMismatchException e) {

                System.out.println(
                        "Invalid input. Please enter the correct data type."
                );

                sc.nextLine();
                choice = 0;
            }

        } while (choice != 15);

        sc.close();
    }

    public static void main(String[] args) {

        SmartHospitalSystem system =
                new SmartHospitalSystem();

        system.menu();
    }
}