DROP TABLE IF EXISTS DishIngredient;
DROP TABLE IF EXISTS Ingredient;
DROP TABLE IF EXISTS Dish;
DROP TABLE IF EXISTS stockmovement;

DROP TYPE IF EXISTS dish_type_enum;
DROP TYPE IF EXISTS category_enum;
DROP TYPE IF EXISTS unit_enum;
DROP TYPE IF EXISTS movement_type_enum;

CREATE TYPE dish_type_enum AS ENUM ('START', 'MAIN', 'DESSERT');
CREATE TYPE category_enum AS ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');
CREATE TYPE unit_enum AS ENUM ('KG', 'L', 'PIECE', 'UNIT');
CREATE TYPE movement_type_enum AS ENUM ('IN', 'OUT');

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

CREATE TABLE stockmovement (
                               id SERIAL PRIMARY KEY,
                               id_ingredient INT NOT NULL,
                               quantity NUMERIC(10, 3) NOT NULL,
                               unit unit_enum NOT NULL DEFAULT 'KG',
                               type movement_type_enum NOT NULL,
                               creation_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_stockmovement_ingredient
                                   FOREIGN KEY (id_ingredient)
                                       REFERENCES Ingredient(id)
                                       ON DELETE CASCADE
);

CREATE INDEX idx_dishingredient_dish ON DishIngredient(id_dish);
CREATE INDEX idx_dishingredient_ingredient ON DishIngredient(id_ingredient);
CREATE INDEX idx_dish_selling_price ON Dish(selling_price);
CREATE INDEX idx_stockmovement_ingredient ON stockmovement(id_ingredient);
CREATE INDEX idx_stockmovement_datetime ON stockmovement(creation_datetime);

CREATE TYPE payment_status_enum AS ENUM ('UNPAID', 'PAID');

CREATE TABLE "order" (
                         id SERIAL PRIMARY KEY,
                         reference VARCHAR(255) NOT NULL UNIQUE,
                         payment_status payment_status_enum NOT NULL DEFAULT 'UNPAID',
                         order_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sale (
                      id SERIAL PRIMARY KEY,
                      order_id INT NOT NULL UNIQUE,
                      sale_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                      CONSTRAINT fk_order
                          FOREIGN KEY (order_id)
                              REFERENCES "order"(id)
                              ON DELETE CASCADE
);

CREATE TABLE order_dish (
                            id SERIAL PRIMARY KEY,
                            order_id INT NOT NULL,
                            dish_id INT NOT NULL,
                            quantity INT NOT NULL DEFAULT 1,

                            CONSTRAINT fk_order_dish_order
                                FOREIGN KEY (order_id)
                                    REFERENCES "order"(id)
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_order_dish_dish
                                FOREIGN KEY (dish_id)
                                    REFERENCES dish(id)
                                    ON DELETE CASCADE,

                            CONSTRAINT unique_order_dish
                                UNIQUE (order_id, dish_id)
);

DELETE from order_dish;
DELETE from "order";
DELETE from sale;

INSERT INTO "order" (id, reference, payment_status)
VALUES (1, '201', 'PAID');

INSERT INTO sale (id, order_id, sale_datetime)
VALUES (1, 1, CURRENT_TIMESTAMP);

INSERT INTO "order" (id, reference, payment_status)
VALUES (2, '202', 'UNPAID');

