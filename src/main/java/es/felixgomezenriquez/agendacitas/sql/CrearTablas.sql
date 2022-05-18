CREATE TABLE EMPRESA (
    ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY,
    CODIGO CHAR(2),
    NOMBRE VARCHAR(20) NOT NULL,
    CONSTRAINT ID_EMPRESA_PK PRIMARY KEY (ID)
);


CREATE TABLE REUNION (
    ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY, -- Id autonumérico
    NOMBRE_REUNION VARCHAR(20) NOT NULL,
    LUGAR_REUNION VARCHAR(30)  NOT NULL,
    FECHA_REUNION DATE,
    TEMAS_A_TRATAR VARCHAR(100)  NOT NULL,
    EMPRESA INTEGER,
    CONSTRAINT ID_REUNION_PK PRIMARY KEY (ID),
    CONSTRAINT EMPRESAREUNION_FK FOREIGN KEY (EMPRESA) REFERENCES EMPRESA (ID)
);
