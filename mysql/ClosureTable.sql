USE db2019;

CREATE TABLE `area` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '区域id',
  `name` varchar(128) DEFAULT NULL COMMENT '区域名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '区域表';

INSERT INTO area(name)
VALUES('A1'),
('A2'),
('A3'),
('A11'),
('A12'),
('A13'),
('A111'),
('A1111'),
('A11111');

CREATE TABLE `area_closure` (
  `ancestor` int(11) NOT NULL COMMENT '祖先节点',
  `descendant` int(11) NOT NULL COMMENT '后代节点',
  `distance` int(11) DEFAULT 0 COMMENT '深度距离',
  PRIMARY KEY (`ancestor`, `descendant`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '区域闭包表'

-- 清空数据
DELETE FROM area_closure WHERE `level` >= 0 ;

INSERT INTO area_closure(ancestor, descendant, `level`)
VALUES(1, 1, 0),
(1, 4, 1),
(1, 5, 1),
(1, 6, 1),
(1, 7, 2),
(1, 8, 3),
(1, 9, 4),
(2, 2, 0),
(3, 3, 0),
(4, 4, 0),
(4, 7, 1),
(4, 8, 2),
(4, 9, 3),
(5, 5, 0),
(6, 6, 0),
(7, 7, 0),
(7, 8, 1),
(7, 9, 2),
(8, 8, 0),
(8, 9, 1),
(9, 9, 0);

-- 新增节点

/* 插入节点 */
INSERT INTO area(name)
VALUES('A111111');
/* 获取自增id */
SELECT LAST_INSERT_ID();
/* 
 * 更新area_closure
 * 根据父节点
 */
INSERT INTO area_closure(ancestor, descendant, `level`)
  SELECT a1.ancestor, 10 AS descendant, (a1.`level` + 1) AS `level`
  FROM area_closure a1
  WHERE a1.descendant = 9
  UNION ALL
  SELECT 10, 10, 0;
 
-- 查询特定节点路径
SELECT a.name 
FROM area_closure ac, area a
WHERE ac.descendant = 10 AND ac.ancestor = a.id 
ORDER BY ac.`level` DESC 

-- 查询特定节点下一级子节点
SELECT a.id, a.name 
FROM area_closure ac, area a
WHERE ac.ancestor = 1 AND ac.descendant = a.id AND ac.`level` = 1

-- 查询特定节点所有子节点
SELECT a.id, a.name 
FROM area_closure ac, area a
WHERE ac.ancestor = 1 AND ac.descendant = a.id
ORDER BY ac.`level` ASC 

-- 查询节点深度
SELECT max(ac.`level`) AS `level` 
FROM area_closure ac
WHERE ac.descendant = 10

-- 删除节点

/*
 * 删除所有节点关系
 */
DELETE FROM area_closure
-- SELECT * FROM area_closure 
WHERE descendant IN (
  SELECT ac.descendant FROM (
    SELECT descendant
    FROM area_closure
    WHERE ancestor = 9
  ) AS ac
);

-- 移动节点
/*
 * 首先，要断开这颗子树和它的祖先们的关系；然后将这颗孤立的树和新节点及它的祖先建立关系
 */
-- SELECT * FROM area_closure 
DELETE FROM area_closure
WHERE descendant IN (
  SELECT a1.descendant FROM (
    SELECT descendant
    FROM area_closure
    WHERE ancestor = 4
  ) AS a1
) AND ancestor IN (
  SELECT a2.ancestor FROM (
    SELECT ancestor
    FROM area_closure
    WHERE descendant = 4 AND ancestor != descendant 
  ) AS a2
);

INSERT INTO area_closure(ancestor, descendant, `level`)
  SELECT supertree.ancestor,  subtree.descendant, (supertree.`level` + subtree.`level` + 1) AS `level` 
  FROM area_closure AS supertree
  CROSS JOIN area_closure AS subtree
  WHERE supertree.descendant = 6 AND subtree.ancestor = 4
  