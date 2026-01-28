TRUNCATE TABLE DishIngredient, Ingredient, Dish RESTART IDENTITY CASCADE;

INSERT INTO Dish (id, name, dish_type, selling_price) VALUES
                                                          (1, 'Salade fraîche', 'START', 3500.00),
                                                          (2, 'Poulet grillé', 'MAIN', 12000.00),
                                                          (3, 'Riz aux légumes', 'MAIN', NULL),
                                                          (4, 'Gâteau au chocolat', 'DESSERT', 8000.00),
                                                          (5, 'Salade de fruits', 'DESSERT', NULL);

INSERT INTO Ingredient (id, name, price, category) VALUES
                                                       (1, 'Laitue', 800.00, 'VEGETABLE'),
                                                       (2, 'Tomate', 600.00, 'VEGETABLE'),
                                                       (3, 'Poulet', 9000.00, 'ANIMAL'),
                                                       (4, 'Chocolat', 3000.00, 'OTHER'),
                                                       (5, 'Beurre', 2500.00, 'DAIRY'),
                                                       (6, 'Huile', 2000.00, 'OTHER'),
                                                       (7, 'Riz', 1500.00, 'OTHER');

INSERT INTO DishIngredient (id_dish, id_ingredient, quantity_required, unit) VALUES
                                                                                 (1, 1, 0.20, 'KG'),
                                                                                 (1, 2, 0.15, 'KG'),
                                                                                 (2, 3, 0.45, 'KG'),
                                                                                 (2, 6, 0.15, 'L'),
                                                                                 (4, 4, 0.30, 'KG'),
                                                                                 (4, 5, 0.20, 'KG');

SELECT setval('dish_id_seq', (SELECT MAX(id) FROM Dish));
SELECT setval('ingredient_id_seq', (SELECT MAX(id) FROM Ingredient));
SELECT setval('dishingredient_id_seq', (SELECT MAX(id) FROM DishIngredient));

INSERT INTO stockmovement (id, id_ingredient, quantity, unit, type, creation_datetime) VALUES
                                                                                           (6, 1, 0.2, 'KG', 'OUT', '2024-01-06 12:00:00'),
                                                                                           (7, 2, 0.15, 'KG', 'OUT', '2024-01-06 12:00:00'),
                                                                                           (8, 3, 1.0, 'KG', 'OUT', '2024-01-06 12:00:00'),
                                                                                           (9, 4, 0.3, 'KG', 'OUT', '2024-01-06 12:00:00'),
                                                                                           (10, 5, 0.2, 'KG', 'OUT', '2024-01-06 12:00:00');

SELECT setval('stockmovement_id_seq', (SELECT MAX(id) FROM stockmovement));