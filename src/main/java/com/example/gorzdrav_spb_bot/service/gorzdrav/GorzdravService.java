package com.example.gorzdrav_spb_bot.service.gorzdrav;

import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.GorzdravClient;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.*;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class GorzdravService {

    private final GorzdravClient gorzdravClient;

    @NonNull
    public List<District> getDistricts() {
        var response = gorzdravClient.getDistricts();
        if (response.success() == null || !response.success()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, response.message());
        }
        return response.result();
    }

    @NonNull
    public List<LPU> getLPUs(District district) {
        var response = gorzdravClient.getLPUs(district);
        if (response.success() == null || !response.success()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, response.message());
        }
        return response.result();
    }

    @NonNull
    public List<Specialty> getSpecialties(LPU lpu) {
        var response = gorzdravClient.getSpecialties(lpu);
        if (response.success() == null || !response.success()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, response.message());
        }
        return response.result();
    }

    @NonNull
    public List<Doctor> getDoctors(Specialty specialty, LPU lpu) {
        var response = gorzdravClient.getDoctors(lpu, specialty);
        if (response.success() == null || !response.success()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, response.message());
        }
        return response.result();
    }

    @NonNull
    public List<Appointment> getAppointments(LPU lpu, Doctor doctor) {
        return getAppointments(lpu.id(), doctor.id());
    }

    @NonNull
    public List<Appointment> getAppointments(String lpuId, String doctorId) {
        var response = gorzdravClient.getAppointments(lpuId, doctorId);
        if (response.success() == null || !response.success()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, response.message());
        }
        return response.result();
    }

    public void createAppointment(Appointment appointment, String lpuId, String patientId) {
        var request = CreateAppointmentRequest.builder()
                .appointmentId(appointment.id())
                .lpuId(lpuId)
                .patientId(patientId).build();
        var response = gorzdravClient.createAppointment(request);
        if (response.success() == null || !response.success()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, response.message());
        }
    }

    public void createAppointment(Appointment appointment, LPU lpu, String patientId) {
        createAppointment(appointment, lpu.id(), patientId);
    }

    @NonNull
    public List<FullAppointment> getFullAppointments(LPU lpu, String patientId) {
        var response = gorzdravClient.findAppointment(lpu, patientId);
        if (response.success() == null || !response.success()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, response.message());
        }
        return response.result();
    }

    public void cancelAppointment(FullAppointment fullAppointment, LPU lpu, String patientId) {
        var request = CancelAppointmentRequest.builder()
                .appointmentId(fullAppointment.appointmentId())
                .lpuId(lpu.id())
                .patientId(patientId).build();
        var response = gorzdravClient.cancelAppointment(request);
        if (response.success() == null || !response.success()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, response.message());
        }
    }

    public String findPatient(LPU lpu, MedicalCard medicalCard) {
        var request = PatientRequest.builder()
                .birthdate(dateToString(medicalCard.getBirthDate()))
                .lastName(medicalCard.getLastName())
                .firstName(medicalCard.getFirstName())
                .middleName(medicalCard.getMiddleName())
                .lpuId(Integer.parseInt(lpu.id()))
                .build();
        var response = gorzdravClient.findPatient(request);
        if (response.success() == null || !response.success()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, response.message());
        }
        return response.result();
    }

    private String dateToString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        return dateFormat.format(date);
    }
}
