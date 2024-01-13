# Estonian Motor Vehicle Tax Calculator

## Description

This Java Spring Boot application calculates the motor vehicle tax for Estonia, as per the new tax law effective from 2023. The program considers the various components such as registration fees and annual taxes for different vehicle types, including exemptions and special cases.

## Features

- **Registration Fee Calculation:** Calculates the one-time registration fee for new vehicles based on CO2 emissions, vehicle weight, and other factors.
- **Annual Tax Calculation:** Computes the annual tax for vehicles considering CO2 emissions, vehicle age, and other relevant parameters.
- **Exemptions Handling:** Accounts for various tax exemptions as outlined in the Estonian motor vehicle tax law.

## Installation

1. Clone the repository: `git clone [repository-link]`
2. Navigate to the project directory: `cd [project-directory]`
3. Build the project: `./mvnw clean install`

## Usage

To run the application:

```shell
java -jar target/taxcalculator-0.0.1-SNAPSHOT.jar
```

## Contributing
Contributions are welcome. Please fork the repository and submit a pull request with your proposed changes.

## License
This project is licensed under the MIT License - see the LICENSE.md file for details.
