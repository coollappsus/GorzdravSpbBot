package com.example.gorzdrav_spb_bot.service.gorzdrav.api;

import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@AllArgsConstructor
public class GorzdravClient {

    private final RestTemplate restTemplate;

    public DistrictsResponse getDistricts() {
        String url = "https://gorzdrav.spb.ru/_api/api/v2/shared/districts";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-type", "application/json");

        HttpEntity<Object> httpEntity = new HttpEntity<>(null, httpHeaders);

        ResponseEntity<DistrictsResponse> responseEntity = restTemplate.exchange(
                url, HttpMethod.GET, httpEntity, DistrictsResponse.class
        );
        return responseEntity.getBody();
    }

    public LPUsResponse getLPUs(District district) {
        String url = "https://gorzdrav.spb.ru/_api/api/v2/shared/district/%d/lpus".formatted(district.id());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-type", "application/json");

        HttpEntity<Object> httpEntity = new HttpEntity<>(null, httpHeaders);

        ResponseEntity<LPUsResponse> responseEntity = restTemplate.exchange(
                url, HttpMethod.GET, httpEntity, LPUsResponse.class
        );
        return responseEntity.getBody();
    }

    public LPUsResponse getAllLPUs() {
        String url = "https://gorzdrav.spb.ru/_api/api/v2/shared/lpus";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-type", "application/json");

        HttpEntity<Object> httpEntity = new HttpEntity<>(null, httpHeaders);

        ResponseEntity<LPUsResponse> responseEntity = restTemplate.exchange(
                url, HttpMethod.GET, httpEntity, LPUsResponse.class
        );
        return Objects.requireNonNull(responseEntity.getBody());
    }

    public SpecialtiesResponse getSpecialties(LPU lpu) {
        String url = "https://gorzdrav.spb.ru/_api/api/v2/schedule/lpu/%s/specialties".formatted(lpu.id());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-type", "application/json");

        HttpEntity<Object> httpEntity = new HttpEntity<>(null, httpHeaders);

        ResponseEntity<SpecialtiesResponse> responseEntity = restTemplate.exchange(
                url, HttpMethod.GET, httpEntity, SpecialtiesResponse.class
        );
        return responseEntity.getBody();
    }

    public DoctorsResponse getDoctors(LPU lpu, Specialty specialty) {
        String url = "https://gorzdrav.spb.ru/_api/api/v2/schedule/lpu/%s/speciality/%s/doctors"
                .formatted(lpu.id(), specialty.id());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-type", "application/json");
        HttpEntity<Object> httpEntity = new HttpEntity<>(null, httpHeaders);

        ResponseEntity<DoctorsResponse> responseEntity = restTemplate.exchange(
                url, HttpMethod.GET, httpEntity, DoctorsResponse.class);

        return responseEntity.getBody();
    }

    public AppointmentsResponse getAppointments(String lpuId, String doctorId) {
        String url = "https://gorzdrav.spb.ru/_api/api/v2/schedule/lpu/%s/doctor/%s/appointments"
                .formatted(lpuId, doctorId);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-type", "application/json");
        HttpEntity<Object> httpEntity = new HttpEntity<>(null, httpHeaders);

        ResponseEntity<AppointmentsResponse> responseEntity = restTemplate.exchange(
                url, HttpMethod.GET, httpEntity, AppointmentsResponse.class);

        return responseEntity.getBody();
    }

    public PatientResponse findPatient(PatientRequest patientRequest) {
        String url = ("https://gorzdrav.spb.ru/_api/api/v2/patient/search?" +
                "lpuId=%s&firstName=%s&middleName=%s&lastName=%s&birthdate=%s")
                .formatted(patientRequest.lpuId(), patientRequest.firstName(), patientRequest.middleName(),
                        patientRequest.lastName(), patientRequest.birthdate());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-type", "application/json");
        HttpEntity<PatientRequest> httpEntity = new HttpEntity<>(null, httpHeaders);

        ResponseEntity<PatientResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, PatientResponse.class);

        return responseEntity.getBody();
    }

    public DefaultResponse createAppointment(CreateAppointmentRequest request) {
        String url = "https://gorzdrav.spb.ru/_api/api/v2/appointment/create";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-type", "application/json");
        HttpEntity<CreateAppointmentRequest> httpEntity = new HttpEntity<>(request, httpHeaders);

        ResponseEntity<DefaultResponse> responseEntity = restTemplate.exchange(
                url, HttpMethod.POST, httpEntity, DefaultResponse.class);

        return responseEntity.getBody();
    }

    public FindAppointmentResponse findAppointment(LPU lpu, String patientId) {
        String url = "https://gorzdrav.spb.ru/_api/api/v2/appointments?lpuId=%s&patientId=%s"
                .formatted(lpu.id(), patientId);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-type", "application/json");
        HttpEntity<FindAppointmentResponse> httpEntity = new HttpEntity<>(null, httpHeaders);

        ResponseEntity<FindAppointmentResponse> responseEntity = restTemplate.exchange(
                url, HttpMethod.GET, httpEntity, FindAppointmentResponse.class);

        return responseEntity.getBody();
    }

    public DefaultResponse cancelAppointment(CancelAppointmentRequest request) {
        String url = "https://gorzdrav.spb.ru/_api/api/v2/appointment/cancel";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-type", "application/json");
        HttpEntity<CancelAppointmentRequest> httpEntity = new HttpEntity<>(request, httpHeaders);

        ResponseEntity<DefaultResponse> responseEntity = restTemplate.exchange(
                url, HttpMethod.POST, httpEntity, DefaultResponse.class);

        return responseEntity.getBody();
    }
}
