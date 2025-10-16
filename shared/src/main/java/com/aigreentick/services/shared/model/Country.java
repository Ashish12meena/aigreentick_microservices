package com.aigreentick.services.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.aigreentick.services.common.model.base.JpaBaseEntity;

@Entity
@Table(name = "countries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Country extends JpaBaseEntity  {

    @Column(unique = true)
    @NotBlank
    private String name;

    @Column(name = "mobile_code", length = 20)
    private String mobileCode;

    @Column(name = "iso_code", length = 5, unique = true)
    private String isoCode; // e.g., "IN" for India, "US" for USA

    @Column(name = "currency_code", length = 5)
    private String currencyCode; // e.g., "INR", "USD"

    @Column(name = "currency_symbol", length = 5)
    private String currencySymbol; // e.g., "₹", "$"

    @Column(name = "time_zone", length = 50)
    private String timeZone; // e.g., "Asia/Kolkata"

    @Column(name = "flag_url")
    private String flagUrl; // URL to country flag image

    @Column(name = "is_active")
    private Boolean isActive = true; // whether this country is active/supported

    @Column(name = "continent", length = 50)
    private String continent; // e.g., "Asia", "Europe"


}


