package ru.netology.selenide;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

class CardDeliveryOrderTest {

    private String planningDate;

    @BeforeEach
    void setUp() {
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 15000;
        Configuration.headless = Boolean.parseBoolean(System.getProperty("selenide.headless", "false"));

        planningDate = LocalDate.now().plusDays(3)
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        java.io.File driverFile = new java.io.File("drivers/chromedriver.exe");
        if (driverFile.exists()) {
            ChromeDriverService service = new ChromeDriverService.Builder()
                    .usingDriverExecutable(driverFile)
                    .build();

            ChromeOptions options = new ChromeOptions();
            if (Configuration.headless) {
                options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
            }

            WebDriverRunner.setWebDriver(new ChromeDriver(service, options));
        }
        // если файла нет (CI) — Selenide сам создаст драйвер через Selenium Manager

        open("http://localhost:9999");
    }

    @Test
    void shouldSubmitOrderForm() {
        // Город
        $("[data-test-id='city'] input").setValue("Москва");

        // Дата: очищаем предзаполненное значение и вводим новую
        $("[data-test-id='date'] input")
                .click();
        $("[data-test-id='date'] input")
                .sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        $("[data-test-id='date'] input").setValue(planningDate);

        // ФИО
        $("[data-test-id='name'] input").setValue("Иванов Иван");

        // Телефон
        $("[data-test-id='phone'] input").setValue("+79991234567");

        // Чекбокс согласия (data-test-id на label)
        $("[data-test-id='agreement']").click();

        // Кнопка "Забронировать"
        $("button.button").click();

        // Проверяем всплывающее окно успеха
        $("[data-test-id='notification']")
                .shouldBe(Condition.visible, Duration.ofSeconds(15))
                .shouldHave(Condition.text("Успешно"), Duration.ofSeconds(15))
                .shouldHave(Condition.text("01.01.1990"), Duration.ofSeconds(15));
    }
}