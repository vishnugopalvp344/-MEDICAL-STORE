package com.example.medicalstore.controllers;

import com.example.medicalstore.models.Medicine;
import com.example.medicalstore.models.User;
import com.example.medicalstore.repository.MedicineRepository;
import com.example.medicalstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/medicines")
public class MedicineController {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String viewMedicines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String keyword,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername()).get();
        Pageable pageable = PageRequest.of(page, 1, Sort.by("createdAt").descending());

        Page<Medicine> medicinePage = keyword.isEmpty()
                ? medicineRepository.findByUser(user, pageable)
                : medicineRepository.findByUserAndNameContainingIgnoreCase(user, keyword, pageable);

        model.addAttribute("medicines", medicinePage.getContent());
        model.addAttribute("totalPages", medicinePage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        return "medicine-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("medicine", new Medicine());
        return "medicine-form";
    }

    @PostMapping("/add")
    public String addMedicine(
            @ModelAttribute Medicine medicine,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername()).get();

        if (medicineRepository.countByUser(user) >= 5) {
            model.addAttribute("error", "You can only add up to 5 medicines.");
            return "medicine-form";
        }

        medicine.setUser(user);
        medicine.setCreatedAt(LocalDateTime.now());
        medicineRepository.save(medicine);
        return "redirect:/medicines";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Medicine med = medicineRepository.findById(id).orElse(null);
        if (med == null || !med.getUser().getEmail().equals(userDetails.getUsername())) {
            return "redirect:/medicines";
        }
        model.addAttribute("medicine", med);
        return "medicine-form";
    }

    @PostMapping("/edit/{id}")
    public String updateMedicine(@PathVariable Long id, @ModelAttribute Medicine medicine,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        Medicine existing = medicineRepository.findById(id).orElse(null);
        if (existing == null || !existing.getUser().getEmail().equals(userDetails.getUsername())) {
            return "redirect:/medicines";
        }

        existing.setName(medicine.getName());
        existing.setStock(medicine.getStock());
        medicineRepository.save(existing);
        return "redirect:/medicines";
    }

    @GetMapping("/delete/{id}")
    public String deleteMedicine(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Medicine med = medicineRepository.findById(id).orElse(null);
        if (med != null && med.getUser().getEmail().equals(userDetails.getUsername())) {
            medicineRepository.delete(med);
        }
        return "redirect:/medicines";
    }
}
