package com.chandan.medical_store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private SaleRepository saleRepository;

    @GetMapping
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    @PostMapping
    public Medicine addMedicine(@RequestBody Medicine medicine) {
        return medicineRepository.save(medicine);
    }

    @DeleteMapping("/{id}")
    public void deleteMedicine(@PathVariable Long id) {
        medicineRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public Medicine updateMedicine(@PathVariable Long id, @RequestBody Medicine medicineDetails) {
        Medicine medicine = medicineRepository.findById(id).orElseThrow();
        medicine.setName(medicineDetails.getName());
        medicine.setCompany(medicineDetails.getCompany());
        medicine.setPrice(medicineDetails.getPrice());
        medicine.setQuantity(medicineDetails.getQuantity());
        medicine.setExpiryDate(medicineDetails.getExpiryDate());
        return medicineRepository.save(medicine);
    }

    public static class BillItem {
        public Long id;
        public int quantity;
    }

    public static class SellBatchRequest {
        public String customerName;
        public String customerPhone;
        public List<BillItem> items;
        public Double totalAmount;
    }

    @PostMapping("/sell-batch")
    public String sellBatchMedicines(@RequestBody SellBatchRequest request) {
        for (BillItem item : request.items) {
            Medicine medicine = medicineRepository.findById(item.id).orElseThrow();
            if (medicine.getQuantity() < item.quantity) {
                throw new RuntimeException("Insufficient stock for: " + medicine.getName());
            }
            medicine.setQuantity(medicine.getQuantity() - item.quantity);
            medicineRepository.save(medicine);
        }

        // Save Sale Record
        Sale sale = new Sale(
            request.customerName.isEmpty() ? "Walk-in Customer" : request.customerName,
            request.customerPhone.isEmpty() ? "N/A" : request.customerPhone,
            request.totalAmount,
            LocalDate.now()
        );
        saleRepository.save(sale);

        return "Bill processed and Sale saved successfully!";
    }

    @GetMapping("/sales")
    public List<Sale> getAllSales() {
        return saleRepository.findAllByOrderByIdDesc();
    }
}