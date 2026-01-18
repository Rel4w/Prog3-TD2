DROP TABLE IF EXISTS DishIngredient;
DROP TABLE IF EXISTS Ingredient;
DROP TABLE IF EXISTS Dish;

DROP TYPE IF EXISTS dish_type_enum;
DROP TYPE IF EXISTS category_enum;
DROP TYPE IF EXISTS unit_enum;

CREATE TYPE dish_type_enum AS ENUM ('START', 'MAIN', 'DESSERT');
CREATE TYPE category_enum AS ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');
CREATE TYPE unit_enum AS ENUM ('KG', 'L', 'PIECE', 'UNIT');

CREATE TABLE Dish (
                      id SERIAL PRIMARY KEY,
                      name VARCHAR(255) NOT NULL,
                      dish_type dish_type_enum NOT NULL,
                      selling_price NUMERIC(10, 2) NULL
);

CREATE TABLE Ingredient (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(255) NOT NULL UNIQUE,
                            price NUMERIC(10, 2) NOT NULL,
                            category category_enum NOT NULL

);

CREATE TABLE DishIngredient (
                                id SERIAL PRIMARY KEY,
                                id_dish INT NOT NULL,
                                id_ingredient INT NOT NULL,
                                quantity_required NUMERIC(10, 3) NOT NULL,
                                unit unit_enum NOT NULL,

                                CONSTRAINT fk_dish
                                    FOREIGN KEY (id_dish)
                                        REFERENCES Dish(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT fk_ingredient
                                    FOREIGN KEY (id_ingredient)
                                        REFERENCES Ingredient(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT unique_dish_ingredient
                                    UNIQUE (id_dish, id_ingredient)
);

CREATE INDEX idx_dishingredient_dish ON DishIngredient(id_dish);
CREATE INDEX idx_dishingredient_ingredient ON DishIngredient(id_ingredient);
CREATE INDEX idx_dish_selling_price ON Dish(selling_price);