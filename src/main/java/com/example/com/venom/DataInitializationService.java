package com.example.com.venom;

import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.entity.Menu.*;
import com.example.com.venom.entity.ReviewEntity;
import com.example.com.venom.entity.TableEntity;
import com.example.com.venom.enums.EstablishmentStatus;
import com.example.com.venom.enums.EstablishmentType;
import com.example.com.venom.repository.EstablishmentRepository;
import com.example.com.venom.repository.Menu.*;
import com.example.com.venom.repository.ReviewRepository;
import com.example.com.venom.repository.TableRepository;
import com.example.com.venom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DataInitializationService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    private final EstablishmentRepository establishmentRepository;
    private final TableRepository tableRepository;
    private final FoodGroupRepository foodGroupRepository;
    private final FoodRepository foodRepository;
    private final DrinksGroupRepository drinksGroupRepository;
    private final DrinkRepository drinkRepository;
    private final DrinkOptionRepository drinkOptionRepository;

    // Базовый путь к изображениям
    private static final String BASE_IMAGE_PATH = "C:\\Users\\vladv\\сервер\\venom\\src\\main\\java\\com\\example\\com\\venom\\images";

    // Варианты расписания
    private static final String[] SCHEDULES = {
            "Пн-Пт 9:00-22:00, Сб-Вс 10:00-23:00",
            "Пн-Вс 8:00-24:00",
            "Пн-Сб 10:00-20:00, Вс 10:00-18:00"
    };

    // Адреса в Минске
    private static final String[] ADDRESSES = {
            "пр-т Независимости, 1", "ул. Ленина, 15", "пр-т Победителей, 23",
            "ул. Немига, 5", "ул. Горького, 28", "пр-т Дзержинского, 104",
            "ул. Козлова, 17", "ул. Мельникайте, 4", "ул. Веры Хоружей, 8"
    };

    // Пути к изображениям по типам заведений
    private static final Map<EstablishmentType, List<String>> PHOTO_PATHS_BY_TYPE = new HashMap<>();

    static {
        // Рестораны
        PHOTO_PATHS_BY_TYPE.put(EstablishmentType.Restaurant, Arrays.asList(
                "establishments\\restaurant\\restaurant1.jpg",
                "establishments\\restaurant\\restaurant2.jpg",
                "establishments\\restaurant\\restaurant3.jpg"
        ));

        // Кафе
        PHOTO_PATHS_BY_TYPE.put(EstablishmentType.Cafe, Arrays.asList(
                "establishments\\cafe\\cafe1.jpg",
                "establishments\\cafe\\cafe2.jpg",
                "establishments\\cafe\\cafe3.jpg"
        ));

        // Бары и пабы
        PHOTO_PATHS_BY_TYPE.put(EstablishmentType.Pub, Arrays.asList(
                "establishments\\pub\\pub1.jpg",
                "establishments\\pub\\pub2.jpg",
                "establishments\\pub\\pub3.jpg"
        ));

        PHOTO_PATHS_BY_TYPE.put(EstablishmentType.Gastropub, Arrays.asList(
                "establishments\\gastropub\\gastropub1.jpg",
                "establishments\\gastropub\\gastropub2.jpg",
                "establishments\\gastropub\\gastropub3.jpg"
        ));

        // Кофейни
        PHOTO_PATHS_BY_TYPE.put(EstablishmentType.CoffeeHouse, Arrays.asList(
                "establishments\\coffee\\coffee1.jpg",
                "establishments\\coffee\\coffee2.jpg",
                "establishments\\coffee\\coffee3.jpg"
        ));

        // Пиццерии
        PHOTO_PATHS_BY_TYPE.put(EstablishmentType.Pizzeria, Arrays.asList(
                "establishments\\pizzeria\\pizzeria1.jpg",
                "establishments\\pizzeria\\pizzeria2.jpg",
                "establishments\\pizzeria\\pizzeria3.jpg"
        ));

        // Суши-бары
        PHOTO_PATHS_BY_TYPE.put(EstablishmentType.SushiBar, Arrays.asList(
                "establishments\\sushi\\sushi1.jpg",
                "establishments\\sushi\\sushi2.jpg",
                "establishments\\sushi\\sushi3.jpg"
        ));

        // Пекарни
        PHOTO_PATHS_BY_TYPE.put(EstablishmentType.Bakery, Arrays.asList(
                "establishments\\bakery\\bakery1.jpg",
                "establishments\\bakery\\bakery2.jpg",
                "establishments\\bakery\\bakery3.jpg"
        ));

        // Кондитерские
        PHOTO_PATHS_BY_TYPE.put(EstablishmentType.Confectionery, Arrays.asList(
                "establishments\\confectionery\\confectionery1.jpg",
                "establishments\\confectionery\\confectionery2.jpg",
                "establishments\\confectionery\\confectionery3.jpg"
        ));

        // Фаст-фуд
        PHOTO_PATHS_BY_TYPE.put(EstablishmentType.FastFood, Arrays.asList(
                "establishments\\fastfood\\fastfood1.jpg",
                "establishments\\fastfood\\fastfood2.jpg",
                "establishments\\fastfood\\fastfood3.jpg"
        ));

        // Чайные дома
        PHOTO_PATHS_BY_TYPE.put(EstablishmentType.TeaHouse, Arrays.asList(
                "establishments\\teahouse\\teahouse1.jpg",
                "establishments\\teahouse\\teahouse2.jpg",
                "establishments\\teahouse\\teahouse3.jpg"
        ));

        // Столовые
        PHOTO_PATHS_BY_TYPE.put(EstablishmentType.Canteen, Arrays.asList(
                "establishments\\canteen\\canteen1.jpg",
                "establishments\\canteen\\canteen2.jpg",
                "establishments\\canteen\\canteen3.jpg"
        ));

        // Гриль-бары
        PHOTO_PATHS_BY_TYPE.put(EstablishmentType.GrillBar, Arrays.asList(
                "establishments\\grillbar\\grillbar1.jpg",
                "establishments\\grillbar\\grillbar2.jpg",
                "establishments\\grillbar\\grillbar3.jpg"
        ));

        // Дайнеры
        PHOTO_PATHS_BY_TYPE.put(EstablishmentType.Diner, Arrays.asList(
                "establishments\\diner\\diner1.jpg",
                "establishments\\diner\\diner2.jpg",
                "establishments\\diner\\diner3.jpg"
        ));

        // Блинные
        PHOTO_PATHS_BY_TYPE.put(EstablishmentType.PancakeHouse, Arrays.asList(
                "establishments\\pancake\\pancake1.jpg",
                "establishments\\pancake\\pancake2.jpg",
                "establishments\\pancake\\pancake3.jpg"
        ));

        // Кафе-мороженое
        PHOTO_PATHS_BY_TYPE.put(EstablishmentType.IceCreamParlor, Arrays.asList(
                "establishments\\icecream\\icecream1.jpg",
                "establishments\\icecream\\icecream2.jpg",
                "establishments\\icecream\\icecream3.jpg"
        ));

        // Фудтраки
        PHOTO_PATHS_BY_TYPE.put(EstablishmentType.FoodTruck, Arrays.asList(
                "establishments\\foodtruck\\foodtruck1.jpg",
                "establishments\\foodtruck\\foodtruck2.jpg",
                "establishments\\foodtruck\\foodtruck3.jpg"
        ));
    }

    // Пути к изображениям для отзывов
    private static final String[] REVIEW_PHOTO_PATHS = {
            "reviews\\review1.jpg",
            "reviews\\review2.jpg",
            "reviews\\review3.jpg",
            "reviews\\review4.jpg",
            "reviews\\review5.jpg",
            "reviews\\review6.jpg",
            "reviews\\review7.jpg",
            "reviews\\review8.jpg",
            "reviews\\review9.jpg",
            "reviews\\review10.jpg",
            "reviews\\review11.jpg",
            "reviews\\review12.jpg",
            "reviews\\review13.jpg",
            "reviews\\review14.jpg",
            "reviews\\review15.jpg",
    };

    // Названия групп еды
    private static final String[] FOOD_GROUP_NAMES = {
            "Салаты", "Супы", "Главные блюда", "Десерты", "Закуски", "Гарниры"
    };

    // Названия групп напитков
    private static final String[] DRINK_GROUP_NAMES = {
            "Холодные напитки", "Горячие напитки", "Алкогольные напитки", "Соки", "Коктейли"
    };

    // Названия блюд для каждой группы
    private static final Map<String, List<String>> FOOD_ITEMS = new HashMap<>();

    static {
        FOOD_ITEMS.put("Салаты", Arrays.asList("Цезарь", "Греческий", "Оливье", "Крабовый"));
        FOOD_ITEMS.put("Супы", Arrays.asList("Борщ", "Солянка", "Грибной", "Куриный"));
        FOOD_ITEMS.put("Главные блюда", Arrays.asList("Стейк", "Рыба на гриле", "Паста", "Плов"));
        FOOD_ITEMS.put("Десерты", Arrays.asList("Тирамису", "Чизкейк", "Мороженое", "Блинчики"));
        FOOD_ITEMS.put("Закуски", Arrays.asList("Брускетта", "Начос", "Картофель фри", "Крылышки"));
        FOOD_ITEMS.put("Гарниры", Arrays.asList("Картофельное пюре", "Овощи на гриле", "Рис", "Гречка"));
    }

    // Описания блюд
    private static final Map<String, String> FOOD_DESCRIPTIONS = new HashMap<>();

    static {
        FOOD_DESCRIPTIONS.put("Цезарь", "Классический салат с курицей, салатом Айсберг, сухариками и соусом Цезарь");
        FOOD_DESCRIPTIONS.put("Греческий", "Салат с помидорами, огурцами, оливками, сыром фета и оливковым маслом");
        FOOD_DESCRIPTIONS.put("Стейк", "Сочный стейк из говядины с овощами гриль");
        FOOD_DESCRIPTIONS.put("Тирамису", "Итальянский десерт с кофе, маскарпоне и какао");
        FOOD_DESCRIPTIONS.put("Борщ", "Традиционный украинский суп со свеклой и сметаной");
        FOOD_DESCRIPTIONS.put("Рыба на гриле", "Свежая рыба, приготовленная на гриле с лимоном и травами");
        FOOD_DESCRIPTIONS.put("Паста", "Итальянская паста с соусом на выбор");
        FOOD_DESCRIPTIONS.put("Чизкейк", "Нежный чизкейк с ягодным соусом");
    }

    // Названия напитков для каждой группы
    private static final Map<String, List<String>> DRINK_ITEMS = new HashMap<>();

    static {
        DRINK_ITEMS.put("Холодные напитки", Arrays.asList("Кола", "Фанта", "Спрайт", "Вода"));
        DRINK_ITEMS.put("Горячие напитки", Arrays.asList("Кофе", "Чай", "Какао", "Глинтвейн"));
        DRINK_ITEMS.put("Алкогольные напитки", Arrays.asList("Пиво", "Вино", "Виски", "Водка"));
        DRINK_ITEMS.put("Соки", Arrays.asList("Апельсиновый сок", "Яблочный сок", "Томатный сок", "Виноградный сок"));
        DRINK_ITEMS.put("Коктейли", Arrays.asList("Мохито", "Маргарита", "Пина Колада", "Дайкири"));
    }

    // Названия заведений по типам
    private static final Map<EstablishmentType, List<String>> ESTABLISHMENT_NAMES_BY_TYPE = new HashMap<>();

    static {
        // Рестораны
        ESTABLISHMENT_NAMES_BY_TYPE.put(EstablishmentType.Restaurant, Arrays.asList(
                "Ресторан 'У озера'", "Ресторан 'Старый город'", "Ресторан 'Морской'",
                "Ресторан 'Восточный'", "Ресторан 'Итальянский'", "Ресторан 'Французский'",
                "Ресторан 'Гриль'", "Ресторан 'Рыбный'", "Ресторан 'Мясной'",
                "Ресторан 'Вегетарианский'", "Ресторан 'Королевский'", "Ресторан 'Охотничий'"
        ));

        // Кафе
        ESTABLISHMENT_NAMES_BY_TYPE.put(EstablishmentType.Cafe, Arrays.asList(
                "Кафе 'Солнечное'", "Кафе 'Домашнее'", "Кафе 'Кофе и книга'",
                "Кафе 'Уют'", "Кафе 'Париж'", "Кафе 'Венское'",
                "Кафе 'Сказка'", "Кафе 'Антураж'", "Кафе 'Лаунж'",
                "Кафе 'Терраса'", "Кафе 'Гармония'", "Кафе 'Ностальжи'"
        ));

        // Бары и пабы
        ESTABLISHMENT_NAMES_BY_TYPE.put(EstablishmentType.Pub, Arrays.asList(
                "Бар 'У Джона'", "Бар 'Паб'", "Бар 'Коктейль'",
                "Бар 'Голубая устрица'", "Бар 'Ирландский'", "Бар 'Дублин'",
                "Бар 'Бочка'", "Бар 'Подвал'", "Бар 'Ржавый гвоздь'",
                "Бар 'Склад'", "Бар 'Ангар'", "Бар 'Порт'"
        ));

        ESTABLISHMENT_NAMES_BY_TYPE.put(EstablishmentType.Gastropub, Arrays.asList(
                "Гастропаб 'Котел'", "Гастропаб 'Крафт'", "Гастропаб 'Склад'",
                "Гастропаб 'Депо'", "Гастропаб 'Амбар'", "Гастропаб 'Лофт'"
        ));

        // Кофейни
        ESTABLISHMENT_NAMES_BY_TYPE.put(EstablishmentType.CoffeeHouse, Arrays.asList(
                "Кофейня 'Аромат'", "Кофейня 'Уютная'", "Кофейня 'Чашка'",
                "Кофейня 'Бин'", "Кофейня 'Эспрессо'", "Кофейня 'Латте'",
                "Кофейня 'Капучино'", "Кофейня 'Мока'", "Кофейня 'Фильтр'",
                "Кофейня 'Зерно'", "Кофейня 'Обжарка'", "Кофейня 'Кофейный дом'"
        ));

        // Пиццерии
        ESTABLISHMENT_NAMES_BY_TYPE.put(EstablishmentType.Pizzeria, Arrays.asList(
                "Пиццерия 'Италия'", "Пиццерия 'Неаполь'", "Пиццерия 'Рим'",
                "Пиццерия 'Милан'", "Пиццерия 'Додо'", "Пиццерия 'Папа Джонс'",
                "Пиццерия 'Пицца Мания'", "Пиццерия 'Чезаре'", "Пиццерия 'Маргарита'"
        ));

        // Суши-бары
        ESTABLISHMENT_NAMES_BY_TYPE.put(EstablishmentType.SushiBar, Arrays.asList(
                "Суши-бар 'Токио'", "Суши-бар 'Осака'", "Суши-бар 'Киото'",
                "Суши-бар 'Якудза'", "Суши-бар 'Сакура'", "Суши-бар 'Фудзи'",
                "Суши-бар 'Ичибан'", "Суши-бар 'Васаби'", "Суши-бар 'Нори'"
        ));

        // Пекарни
        ESTABLISHMENT_NAMES_BY_TYPE.put(EstablishmentType.Bakery, Arrays.asList(
                "Пекарня 'Вкусная'", "Пекарня 'Сдобная'", "Пекарня 'Булочная'",
                "Пекарня 'Каравай'", "Пекарня 'Батон'", "Пекарня 'Круассан'",
                "Пекарня 'Багет'", "Пекарня 'Пирожок'", "Пекарня 'Булочки'"
        ));

        // Кондитерские
        ESTABLISHMENT_NAMES_BY_TYPE.put(EstablishmentType.Confectionery, Arrays.asList(
                "Кондитерская 'Сладость'", "Кондитерская 'Десерт'", "Кондитерская 'Торт'",
                "Кондитерская 'Пирожное'", "Кондитерская 'Шоколад'", "Кондитерская 'Крем'",
                "Кондитерская 'Медовик'", "Кондитерская 'Наполеон'", "Кондитерская 'Эклер'"
        ));

        // Фаст-фуд
        ESTABLISHMENT_NAMES_BY_TYPE.put(EstablishmentType.FastFood, Arrays.asList(
                "Бургерная 'Гриль'", "Бургерная 'Кинг'", "Бургерная 'Фри'",
                "Фаст-фуд 'Быстро'", "Фаст-фуд 'Вкусно'", "Фаст-фуд 'Скорее'",
                "Шаурма 'Восточная'", "Хот-дог 'Американский'", "Кебаб 'Турецкий'"
        ));

        // Чайные дома
        ESTABLISHMENT_NAMES_BY_TYPE.put(EstablishmentType.TeaHouse, Arrays.asList(
                "Чайный дом 'Восточный'", "Чайный дом 'Китайский'", "Чайный дом 'Японский'",
                "Чайный дом 'Улун'", "Чайный дом 'Пуэр'", "Чайный дом 'Зеленый'",
                "Чайный дом 'Белый'", "Чайный дом 'Красный'", "Чайный дом 'Травяной'"
        ));

        // Для остальных типов используем общие названия
        List<String> commonNames = Arrays.asList(
                "Заведение 'Уют'", "Заведение 'Комфорт'", "Заведение 'Гость'",
                "Заведение 'Радушие'", "Заведение 'Гостеприимство'", "Заведение 'Тепло'",
                "Заведение 'Свет'", "Заведение 'Очаг'", "Заведение 'Приют'"
        );

        for (EstablishmentType type : EstablishmentType.values()) {
            if (!ESTABLISHMENT_NAMES_BY_TYPE.containsKey(type)) {
                ESTABLISHMENT_NAMES_BY_TYPE.put(type, commonNames);
            }
        }
    }

    // Описания заведений по типам
    private static final Map<EstablishmentType, List<String>> ESTABLISHMENT_DESCRIPTIONS_BY_TYPE = new HashMap<>();

    static {
        // Рестораны
        ESTABLISHMENT_DESCRIPTIONS_BY_TYPE.put(EstablishmentType.Restaurant, Arrays.asList(
                "Премиум ресторан для особых случаев с авторской кухней",
                "Элегантный ресторан с изысканным интерьером и винной картой",
                "Ресторан высокой кухни с шеф-поваром международного класса",
                "Традиционный ресторан с национальной кухней и живой музыкой",
                "Современный ресторан с фьюжн-кухней и панорамным видом"
        ));

        // Кафе
        ESTABLISHMENT_DESCRIPTIONS_BY_TYPE.put(EstablishmentType.Cafe, Arrays.asList(
                "Уютное кафе с домашней атмосферой и свежей выпечкой",
                "Современное кафе с бесплатным Wi-Fi и розетками для работы",
                "Семейное кафе с детской комнатой и специальным меню для детей",
                "Кафе в стиле лофт с индустриальным интерьером и арт-выставками",
                "Ретро-кафе с винтажной мебелью и классической музыкой"
        ));

        // Бары и пабы
        ESTABLISHMENT_DESCRIPTIONS_BY_TYPE.put(EstablishmentType.Pub, Arrays.asList(
                "Аутентичный ирландский паб с живой музыкой и крафтовым пивом",
                "Бар с широким выбором коктейлей и уютной атмосферой",
                "Спорт-бар с большими экранами для трансляций матчей",
                "Бар-лаунж с диджейскими сетами и танцполом",
                "Подвальный бар в индустриальном стиле с металлической музыкой"
        ));

        // Кофейни
        ESTABLISHMENT_DESCRIPTIONS_BY_TYPE.put(EstablishmentType.CoffeeHouse, Arrays.asList(
                "Кофейня с собственной обжаркой зерен и профессиональными бариста",
                "Минималистичная кофейня с акцентом на качество кофе",
                "Кофейня-библиотека с тихой атмосферой для работы и чтения",
                "Японская кофейня с традиционными методами заваривания",
                "Кофейня с веганскими десертами и альтернативным молоком"
        ));

        // Пиццерии
        ESTABLISHMENT_DESCRIPTIONS_BY_TYPE.put(EstablishmentType.Pizzeria, Arrays.asList(
                "Неаполитанская пиццерия с дровяной печью и итальянскими продуктами",
                "Семейная пиццерия с детской площадкой и игровой зоной",
                "Пиццерия-буфет с неограниченными добавками пиццы",
                "Пиццерия с авторскими рецептами и необычными начинками",
                "Пиццерия быстрого питания с доставкой за 30 минут"
        ));

        // Суши-бары
        ESTABLISHMENT_DESCRIPTIONS_BY_TYPE.put(EstablishmentType.SushiBar, Arrays.asList(
                "Суши-бар с японским шеф-поваром и свежими морепродуктами",
                "Суши-ресторан с конвейерной лентой и цветовой системой ценообразования",
                "Суши-бар с фьюжн-роллами и азиатской кухней",
                "Минималистичный суши-бар с акцентом на традиционные техники",
                "Суши-бар с открытой кухней, где можно наблюдать за приготовлением"
        ));

        // Общие описания для остальных типов
        List<String> commonDescriptions = Arrays.asList(
                "Уютное место с теплой атмосферой и внимательным обслуживанием",
                "Современное заведение с демократичными ценами и быстрым обслуживанием",
                "Семейное заведение с меню для всех возрастов и детской зоной",
                "Заведение в уникальном стиле с авторским подходом к кухне",
                "Место для встреч с друзьями в расслабленной атмосфере"
        );

        for (EstablishmentType type : EstablishmentType.values()) {
            if (!ESTABLISHMENT_DESCRIPTIONS_BY_TYPE.containsKey(type)) {
                ESTABLISHMENT_DESCRIPTIONS_BY_TYPE.put(type, commonDescriptions);
            }
        }
    }

    // Размеры опций для напитков (в мл)
    private static final int[] DRINK_SIZES = {250, 330, 500, 1000};

    // Количество мест для столиков
    private static final int[] TABLE_CAPACITIES = {2, 4, 6};

    // Количество столиков каждого типа
    private static final int[] TABLE_COUNTS = {2, 5, 1}; // 2 двухместных, 5 четырехместных, 1 шестиместный

    // Типы заведений
    private static final EstablishmentType[] ESTABLISHMENT_TYPES = EstablishmentType.values();

    // Кэш для Base64 изображений
    private final Map<String, String> imageBase64Cache = new HashMap<>();

    /**
     * Загружает изображение и конвертирует в Base64 строку (чистую, без префиксов)
     */
    private String loadImageAsBase64(String relativePath) {
        try {
            // Проверяем кэш
            if (imageBase64Cache.containsKey(relativePath)) {
                return imageBase64Cache.get(relativePath);
            }

            // Формируем абсолютный путь
            String absolutePath = BASE_IMAGE_PATH + "\\" + relativePath;
            Path path = Paths.get(absolutePath);

            if (Files.exists(path)) {
                // Читаем байты файла
                byte[] imageBytes = Files.readAllBytes(path);
                // Конвертируем в Base64
                String base64 = Base64.getEncoder().encodeToString(imageBytes);
                // Сохраняем в кэш
                imageBase64Cache.put(relativePath, base64);
                return base64;
            } else {
                // Если файл не найден, создаем простой placeholder
                System.err.println("Файл не найден: " + absolutePath);
                String placeholder = createSimplePlaceholder(relativePath);
                imageBase64Cache.put(relativePath, placeholder);
                return placeholder;
            }
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке изображения: " + relativePath + " - " + e.getMessage());
            String placeholder = createSimplePlaceholder(relativePath);
            imageBase64Cache.put(relativePath, placeholder);
            return placeholder;
        }
    }

    /**
     * Создает простой Base64 placeholder (черный квадрат)
     */
    private String createSimplePlaceholder(String name) {
        try {
            // Создаем простой черный квадрат 100x100 пикселей в формате PNG
            byte[] placeholderBytes = new byte[100 * 100 * 3]; // RGB, 100x100
            Arrays.fill(placeholderBytes, (byte) 0); // Черный цвет

            // В реальности здесь нужно генерировать PNG, но для простоты используем заглушку
            // Для теста вернем простую строку, которую можно распознать как placeholder
            return "PLACEHOLDER_FOR_" + name.replace("\\", "_").replace(" ", "_");
        } catch (Exception e) {
            return "ERROR_LOADING_IMAGE";
        }
    }

    /**
     * Публичный метод для инициализации тестовых данных.
     * Создает 100 заведений со столиками, меню и отзывами.
     */
    @Transactional
    public void initializeData() {
        // Проверяем, есть ли уже заведения
        if (establishmentRepository.count() == 0) {
            System.out.println("\n🚀 Начало создания тестовых данных...");
            createEstablishments();
            System.out.println("✅ Заведения созданы!");
        } else {
            System.out.println("⚠️ Заведения уже существуют. Пропускаем создание.");
        }

        // Создаем отзывы (если их еще нет)
        generateReviewsForEstablishments();

        System.out.println("\n🎉 Инициализация данных завершена!");
        System.out.println("   - Заведения: " + establishmentRepository.count());
        System.out.println("   - Отзывы: " + reviewRepository.count());
        System.out.println("   - Пользователи: " + userRepository.count());
    }

    /**
     * Принудительно пересоздает все тестовые данные (удаляет старые и создает новые).
     * Используйте с осторожностью!
     */
    @Transactional
    public void reinitializeData() {
        System.out.println("\nНачинаем ПЕРЕСОЗДАНИЕ тестовых данных...");

        // Удаляем все существующие данные (в правильном порядке из-за внешних ключей)
        System.out.println("Очистка старых данных...");
        drinkOptionRepository.deleteAll();
        drinkRepository.deleteAll();
        drinksGroupRepository.deleteAll();
        foodRepository.deleteAll();
        foodGroupRepository.deleteAll();
        tableRepository.deleteAll();
        establishmentRepository.deleteAll();

        // Очищаем кэш изображений
        imageBase64Cache.clear();

        // Создаем новые данные
        createEstablishments();
        System.out.println("✅ Данные успешно пересозданы! Создано 100 заведений.");
    }

    private void createEstablishments() {
        Random random = new Random();

        // Map для отслеживания использованных названий по типам
        Map<EstablishmentType, Set<String>> usedNames = new HashMap<>();

        // Создаем 100 заведений
        for (int i = 0; i < 100; i++) {
            // Создаем заведение
            EstablishmentEntity establishment = new EstablishmentEntity();

            // Выбираем тип заведения
            EstablishmentType type;
            if (i < 20) {
                // Первые 20 - кофейни
                type = EstablishmentType.CoffeeHouse;
            } else if (i < 40) {
                // Следующие 20 - рестораны
                type = EstablishmentType.Restaurant;
            } else if (i < 60) {
                // Следующие 20 - кафе
                type = EstablishmentType.Cafe;
            } else {
                // Остальные - случайные заведения
                type = ESTABLISHMENT_TYPES[random.nextInt(ESTABLISHMENT_TYPES.length)];
            }

            // Выбираем название для данного типа
            List<String> availableNames = ESTABLISHMENT_NAMES_BY_TYPE.get(type);
            Set<String> usedForType = usedNames.computeIfAbsent(type, k -> new HashSet<>());

            String name;
            if (usedForType.size() < availableNames.size()) {
                // Выбираем случайное неиспользованное название
                List<String> unusedNames = new ArrayList<>(availableNames);
                unusedNames.removeAll(usedForType);
                name = unusedNames.get(random.nextInt(unusedNames.size()));
                usedForType.add(name);
            } else {
                // Все названия использованы, добавляем номер
                name = availableNames.get(random.nextInt(availableNames.size())) + " №" + (usedForType.size() + 1);
                usedForType.add(name);
            }

            // Выбираем описание для данного типа
            List<String> descriptions = ESTABLISHMENT_DESCRIPTIONS_BY_TYPE.get(type);
            String description = descriptions.get(random.nextInt(descriptions.size()));

            establishment.setName(name);
            establishment.setDescription(description);
            establishment.setAddress(ADDRESSES[random.nextInt(ADDRESSES.length)] + ", к." + (random.nextInt(10) + 1));
            establishment.setType(type);

            // Генерация случайных координат в Минске
            // Широта: 53.85 - 53.95
            // Долгота: 27.45 - 27.70
            double latitude = 53.85 + random.nextDouble() * 0.1;
            double longitude = 27.45 + random.nextDouble() * 0.25;

            establishment.setLatitude(latitude);
            establishment.setLongitude(longitude);

            // Случайное расписание
            establishment.setOperatingHoursString(SCHEDULES[random.nextInt(SCHEDULES.length)]);

            // Статус и рейтинг
            establishment.setStatus(EstablishmentStatus.ACTIVE);
            establishment.setRating(3.5 + random.nextDouble() * 1.5); // Рейтинг от 3.5 до 5.0

            // ID создателя (заглушка)
            establishment.setCreatedUserId(1L);

            // ID меню (пока null, свяжем позже если нужно)
            establishment.setIdMenu(null);

            // Загружаем фото и конвертируем в Base64
            List<String> photoBase64s = new ArrayList<>();
            List<String> relativePaths = PHOTO_PATHS_BY_TYPE.get(type);

            if (relativePaths != null && !relativePaths.isEmpty()) {
                // Перемешиваем пути для разнообразия
                List<String> shuffledPaths = new ArrayList<>(relativePaths);
                Collections.shuffle(shuffledPaths, random);

                // Загружаем каждое изображение и конвертируем в Base64
                for (String relativePath : shuffledPaths) {
                    String base64 = loadImageAsBase64(relativePath);
                    photoBase64s.add(base64);
                }
            } else {
                // Если нет фото для этого типа, создаем 3 заглушки
                for (int j = 0; j < 3; j++) {
                    String placeholder = createSimplePlaceholder(type.name() + "_" + j);
                    photoBase64s.add(placeholder);
                }
            }

            establishment.setPhotoBase64s(photoBase64s);

            // Сохраняем заведение
            establishment = establishmentRepository.save(establishment);

            // Создаем столики для заведения
            createTablesForEstablishment(establishment, random);

            // Создаем меню для заведения
            createMenuForEstablishment(establishment, random);

            if (i % 10 == 0) {
                System.out.println("  Создано заведений: " + i + "/100");
            }
        }
        System.out.println("  Создано заведений: 100/100");
    }

    private void createTablesForEstablishment(EstablishmentEntity establishment, Random random) {
        List<TableEntity> tables = new ArrayList<>();

        // Для каждого типа столиков (по количеству мест) создаем указанное количество
        for (int i = 0; i < TABLE_CAPACITIES.length; i++) {
            int capacity = TABLE_CAPACITIES[i];
            int count = TABLE_COUNTS[i];

            for (int j = 0; j < count; j++) {
                TableEntity table = new TableEntity();
                table.setEstablishmentId(establishment.getId());
                table.setName("Столик №" + (tables.size() + 1));
                table.setDescription("Столик на " + capacity + " человека");
                table.setMaxCapacity(capacity);
                tables.add(table);
            }
        }

        // Сохраняем столики
        tableRepository.saveAll(tables);
    }

    private void createMenuForEstablishment(EstablishmentEntity establishment, Random random) {
        Long establishmentId = establishment.getId();

        // Определяем, кофейня это или нет
        boolean isCafe = establishment.getType() == EstablishmentType.CoffeeHouse;

        // Создаем группы еды (3-4 группы)
        int foodGroupsCount = isCafe ? 3 : 3 + random.nextInt(2); // 3 или 4
        List<FoodGroupEntity> foodGroups = new ArrayList<>();

        for (int i = 0; i < foodGroupsCount; i++) {
            FoodGroupEntity foodGroup = new FoodGroupEntity();
            foodGroup.setEstablishmentId(establishmentId);
            foodGroup.setName(FOOD_GROUP_NAMES[i]);
            foodGroup = foodGroupRepository.save(foodGroup);
            foodGroups.add(foodGroup);

            // Создаем блюда в группе (3-4 блюда)
            createFoodItemsForGroup(foodGroup, random);
        }

        // Создаем группы напитков (3-4 группы)
        int drinkGroupsCount = isCafe ? 4 : 3 + random.nextInt(2); // Кофейни больше напитков, 3 или 4 у остальных
        List<DrinksGroupEntity> drinkGroups = new ArrayList<>();

        for (int i = 0; i < drinkGroupsCount; i++) {
            DrinksGroupEntity drinksGroup = new DrinksGroupEntity();
            drinksGroup.setEstablishmentId(establishmentId);
            drinksGroup.setName(DRINK_GROUP_NAMES[i]);
            drinksGroup = drinksGroupRepository.save(drinksGroup);
            drinkGroups.add(drinksGroup);

            // Создаем напитки в группе (3-4 напитка)
            createDrinkItemsForGroup(drinksGroup, random);
        }
    }

    private void createFoodItemsForGroup(FoodGroupEntity foodGroup, Random random) {
        String groupName = foodGroup.getName();
        List<String> items = FOOD_ITEMS.getOrDefault(groupName, Arrays.asList("Блюдо 1", "Блюдо 2", "Блюдо 3"));

        // Создаем копию списка и перемешиваем для случайного выбора
        List<String> shuffledItems = new ArrayList<>(items);
        Collections.shuffle(shuffledItems, random);

        // Создаем 3-4 блюда в группе
        int itemsCount = 3 + random.nextInt(2); // 3 или 4

        for (int i = 0; i < itemsCount; i++) {
            FoodEntity food = new FoodEntity();
            food.setFoodGroupId(foodGroup.getId());

            // Берем название из перемешанного списка
            String itemName = shuffledItems.get(i % shuffledItems.size());
            food.setName(itemName);

            // Пищевая ценность
            food.setCaloriesPer100g(round(100 + random.nextDouble() * 200, 1)); // 100-300 калорий
            food.setFatPer100g(round(5 + random.nextDouble() * 20, 1)); // 5-25 г жиров
            food.setCarbohydratesPer100g(round(10 + random.nextDouble() * 50, 1)); // 10-60 г углеводов
            food.setProteinPer100g(round(5 + random.nextDouble() * 30, 1)); // 5-35 г белков

            // Описание
            food.setIngredients(FOOD_DESCRIPTIONS.getOrDefault(
                    itemName,
                    "Свежие ингредиенты, приготовленные с любовью"
            ));

            // Цена - округляем до 2 знаков после запятой
            food.setCost(round(5 + random.nextDouble() * 20, 2)); // 5-25 рублей

            // Вес - округляем до ближайшего кратного 10
            int weight = 200 + random.nextInt(300); // 200-500 грамм
            weight = Math.round(weight / 10.0f) * 10;
            food.setWeight(weight);

            // Для фото блюд используем простые заглушки
            food.setPhotoBase64(createSimplePlaceholder("food_" + itemName));

            foodRepository.save(food);
        }
    }

    private void createDrinkItemsForGroup(DrinksGroupEntity drinksGroup, Random random) {
        String groupName = drinksGroup.getName();
        List<String> items = DRINK_ITEMS.getOrDefault(groupName, Arrays.asList("Напиток 1", "Напиток 2", "Напиток 3"));

        // Создаем копию списка и перемешиваем для случайного выбора
        List<String> shuffledItems = new ArrayList<>(items);
        Collections.shuffle(shuffledItems, random);

        // Создаем 3-4 напитка в группе
        int itemsCount = 3 + random.nextInt(2); // 3 или 4

        for (int i = 0; i < itemsCount; i++) {
            DrinkEntity drink = new DrinkEntity();
            drink.setDrinkGroupId(drinksGroup.getId());

            // Берем название из перемешанного списка
            String itemName = shuffledItems.get(i % shuffledItems.size());
            drink.setName(itemName);

            // Пищевая ценность (для напитков обычно меньше)
            drink.setCaloriesPer100g(round(random.nextDouble() * 100, 1)); // 0-100 калорий
            drink.setFatPer100g(round(random.nextDouble() * 2, 1)); // 0-2 г жиров
            drink.setCarbohydratesPer100g(round(random.nextDouble() * 20, 1)); // 0-20 г углеводов
            drink.setProteinPer100g(round(random.nextDouble() * 2, 1)); // 0-2 г белков

            // Состав
            drink.setIngredients("Натуральные ингредиенты, без консервантов");

            // Для фото напитков используем простые заглушки
            drink.setPhotoBase64(createSimplePlaceholder("drink_" + itemName));

            drink = drinkRepository.save(drink);

            // Создаем опции для напитка (разные размеры и цены)
            createDrinkOptions(drink, random);
        }
    }

    private void createDrinkOptions(DrinkEntity drink, Random random) {
        // Создаем 2-3 опции для напитка
        int optionsCount = 2 + random.nextInt(2); // 2 или 3

        for (int i = 0; i < optionsCount; i++) {
            DrinkOptionEntity option = new DrinkOptionEntity();
            option.setDrinkId(drink.getId());
            option.setSizeMl(DRINK_SIZES[i % DRINK_SIZES.length]);

            // Цена - округляем до 2 знаков после запятой
            option.setCost(round(2 + random.nextDouble() * 10, 2)); // 2-12 рублей

            drinkOptionRepository.save(option);
        }
    }

    /**
     * Метод для округления чисел с заданной точностью
     * @param value исходное значение
     * @param places количество знаков после запятой
     * @return округленное значение
     */
    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Метод для создания тестовых отзывов для всех заведений
     */
    @Transactional
    public void generateReviewsForEstablishments() {
        if (reviewRepository.count() > 0) {
            System.out.println("⚠️ Отзывы уже существуют. Пропускаем создание отзывов.");
            return;
        }

        System.out.println("\nСоздание тестовых отзывов...");

        Random random = new Random();
        List<ReviewEntity> allReviews = new ArrayList<>();

        // Получаем все заведения
        List<EstablishmentEntity> establishments = establishmentRepository.findAll();

        // ID пользователя для отзывов (второй администратор)
        Long reviewerUserId = 2L;

        // Тексты отзывов
        String[] reviewTexts = {
                "Очень понравилось! Приятная атмосфера и вежливый персонал.",
                "Неплохое место, но есть куда расти. Еда была нормальной.",
                "Отличное заведение! Обязательно вернусь с друзьями.",
                "Средненько, ничего особенного. Цены немного завышены.",
                "Восхитительно! Лучшее место в городе, рекомендую всем!",
                "Не ожидал такого качества за такие деньги. Приятно удивлен!",
                "Было неплохо, но обслуживание могло быть и лучше.",
                "Атмосфера просто супер! Идеальное место для вечера с друзьями.",
                "Еда была вкусной, но порции могли бы быть больше.",
                "Уютное место с интересным интерьером. Порадовало меню.",
                "Советую попробовать их фирменные блюда - это нечто!",
                "Цены соответствуют качеству. Будем приходить еще.",
                "Не самый лучший опыт, но и не худший. Попробуйте сами.",
                "Персонал очень внимательный, все понравилось!",
                "Неплохой выбор напитков и закусок. Вечер удался!"
        };

        // Рейтинги с распределением (больше хороших отзывов)
        float[] possibleRatings = {4.5f, 5.0f, 4.0f, 3.5f, 4.0f, 4.5f, 3.0f, 5.0f, 4.0f, 3.5f, 2.5f, 1.5f};

        int totalReviews = 0;

        for (EstablishmentEntity establishment : establishments) {
            // Для каждого заведения создаем 10-15 отзывов
            int reviewsForThisEstablishment = 10 + random.nextInt(6); // 10-15

            for (int i = 0; i < reviewsForThisEstablishment; i++) {
                ReviewEntity review = new ReviewEntity();
                review.setEstablishmentId(establishment.getId());
                review.setCreatedUserId(reviewerUserId);
                review.setRating(possibleRatings[random.nextInt(possibleRatings.length)]);
                review.setReviewText(reviewTexts[random.nextInt(reviewTexts.length)]);

                // Дата отзыва - случайная дата в последние 30 дней
                review.setDateOfCreation(LocalDateTime.now().minusDays(random.nextInt(30)));

                // Шанс 1/6 что отзыв будет без картинки
                if (random.nextInt(6) != 0) { // 5/6 с картинкой
                    // Берем случайное изображение из папки reviews
                    String relativePath = REVIEW_PHOTO_PATHS[random.nextInt(REVIEW_PHOTO_PATHS.length)];
                    String base64 = loadImageAsBase64(relativePath);

                    // Проверяем, что это валидный Base64 (не placeholder)
                    if (!base64.startsWith("PLACEHOLDER") && !base64.startsWith("ERROR")) {
                        review.setPhotoBase64(base64);
                    } else {
                        // Если файл не найден, не ставим фото
                        review.setPhotoBase64(null);
                    }
                } else {
                    review.setPhotoBase64(null);
                }

                allReviews.add(review);
                totalReviews++;
            }

            // Пакетное сохранение каждые 50 отзывов
            if (allReviews.size() >= 50) {
                reviewRepository.saveAll(allReviews);
                allReviews.clear();
                System.out.println("  Создано отзывов: " + totalReviews);
            }
        }

        // Сохраняем оставшиеся отзывы
        if (!allReviews.isEmpty()) {
            reviewRepository.saveAll(allReviews);
        }

        System.out.println("✅ Создано " + totalReviews + " отзывов для " + establishments.size() + " заведений");

        // Обновляем рейтинги заведений на основе отзывов
        updateEstablishmentRatings();
    }

    /**
     * Обновляет рейтинги заведений на основе созданных отзывов
     */
    @Transactional
    public void updateEstablishmentRatings() {
        System.out.println("\nОбновление рейтингов заведений на основе отзывов...");

        List<EstablishmentEntity> establishments = establishmentRepository.findAll();
        int updatedCount = 0;

        for (EstablishmentEntity establishment : establishments) {
            List<ReviewEntity> reviews = reviewRepository.findByEstablishmentId(establishment.getId());

            if (!reviews.isEmpty()) {
                // Вычисляем средний рейтинг
                double sum = 0;
                for (ReviewEntity review : reviews) {
                    sum += review.getRating();
                }
                double averageRating = sum / reviews.size();

                // Округляем до 1 знака после запятой
                establishment.setRating(round(averageRating, 1));
                establishmentRepository.save(establishment);
                updatedCount++;
            }
        }

        System.out.println("✅ Обновлены рейтинги для " + updatedCount + " заведений");
    }

    /**
     * Создает простой SVG с заданным цветом
     */
    private String createColorSvg(String color) {
        String svg = String.format(
                "<svg xmlns='http://www.w3.org/2000/svg' width='200' height='150'>" +
                        "<rect width='200' height='150' fill='%s'/>" +
                        "<text x='100' y='75' font-family='Arial' font-size='14' fill='white' " +
                        "text-anchor='middle' dominant-baseline='middle'>Review Photo</text>" +
                        "</svg>",
                color
        );
        return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.getBytes());
    }
}