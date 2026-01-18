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
                                                       (3, 'Poulet', 9000.00, 'ANIMAL'),  -- 9000 * 0.5 = 4500
                                                       (4, 'Chocolat', 3000.00, 'OTHER'),
                                                       (5, 'Beurre', 2500.00, 'DAIRY'),
                                                       (6, 'Huile', 2000.00, 'OTHER'),
                                                       (7, 'Riz', 1500.00, 'OTHER');

INSERT INTO DishIngredient (id_dish, id_ingredient, quantity_required, unit) VALUES
                                                                                 (1, 1, 0.20, 'KG'),   -- Salade fraîche: 0.20 KG de Laitue (800 * 0.20 = 160)
                                                                                 (1, 2, 0.15, 'KG'),   -- Salade fraîche: 0.15 KG de Tomate (600 * 0.15 = 90) TOTAL: 250
                                                                                 (2, 3, 0.50, 'KG'),   -- Poulet grillé: 0.50 KG de Poulet (9000 * 0.5 = 4500)
                                                                                 (2, 6, 0.15, 'L'),    -- Poulet grillé: 0.15 L d'Huile (2000 * 0.15 = 300) TOTAL: 4800
                                                                                 (3, 7, 1.00, 'KG'),   -- Riz aux légumes: 1.00 KG de Riz (1500 * 1 = 1500)
                                                                                 (4, 4, 0.30, 'KG'),   -- Gâteau chocolat: 0.30 KG de Chocolat (3000 * 0.3 = 900)
                                                                                 (4, 5, 0.20, 'KG');   -- Gâteau chocolat: 0.20 KG de Beurre (2500 * 0.2 = 500) TOTAL: 1400

SELECT setval('dish_id_seq', (SELECT MAX(id) FROM Dish));
SELECT setval('ingredient_id_seq', (SELECT MAX(id) FROM Ingredient));
SELECT setval('dishingredient_id_seq', (SELECT MAX(id) FROM DishIngredient));