package com.eaglebank.eaglebankdomain.user;

import com.eaglebank.eaglebankdomain.exception.InvalidUserDataException;
import java.util.Objects;

public record Address(
    String line1,
    String town,
    String county,
    String postcode
) {
    public Address {
        // Validate line1
        Objects.requireNonNull(line1, "Address line 1 cannot be null");
        if (line1.trim().isEmpty()) {
            throw new InvalidUserDataException("Address line 1 cannot be empty");
        }
        if (line1.length() > 100) {
            throw new InvalidUserDataException("Address line 1 must be 100 characters or less");
        }

        // Validate town
        Objects.requireNonNull(town, "Town cannot be null");
        if (town.trim().isEmpty()) {
            throw new InvalidUserDataException("Town cannot be empty");
        }
        if (town.length() > 50) {
            throw new InvalidUserDataException("Town must be 50 characters or less");
        }

        // Validate county
        Objects.requireNonNull(county, "County cannot be null");
        if (county.trim().isEmpty()) {
            throw new InvalidUserDataException("County cannot be empty");
        }
        if (county.length() > 50) {
            throw new InvalidUserDataException("County must be 50 characters or less");
        }

        // Validate postcode
        Objects.requireNonNull(postcode, "Postcode cannot be null");
        String trimmedPostcode = postcode.trim().toUpperCase();
        if (trimmedPostcode.isEmpty()) {
            throw new InvalidUserDataException("Postcode cannot be empty");
        }
        if (!trimmedPostcode.matches("^[A-Z]{1,2}[0-9R][0-9A-Z]? [0-9][A-Z]{2}$")) {
            throw new InvalidUserDataException("Invalid UK postcode format");
        }

        // Assign trimmed values
        line1 = line1.trim();
        town = town.trim();
        county = county.trim();
        postcode = trimmedPostcode;
    }
}