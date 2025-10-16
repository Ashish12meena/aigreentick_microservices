package com.aigreentick.services.common.model.embedded.contacts;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Address object for shared library.
 * No JPA or validation annotations here.
 * Pure POJO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    private String street;
    private String city;
    private String state;
    private String country;
    private String zipCode;

    /**
     * Convenience method to get full address
     */
    public String getFullAddress() {
        return String.format("%s, %s, %s, %s - %s", street, city, state, country, zipCode);
    }
}
