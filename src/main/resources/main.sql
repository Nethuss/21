CREATE TABLE IF NOT EXISTS public.role
(
    id integer NOT NULL DEFAULT nextval('role_id_seq'::regclass),
    name character varying(100) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT role_pkey PRIMARY KEY (id),
    CONSTRAINT role_name_key UNIQUE (name)
    )


CREATE TABLE IF NOT EXISTS public.user_roles
(
    user_id bigint NOT NULL,
    role_id integer NOT NULL,
    CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id)
    REFERENCES public.role (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id)
    REFERENCES public.users (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE
    )


-- Table: public.users

-- DROP TABLE IF EXISTS public.users;

CREATE TABLE IF NOT EXISTS public.users
(
    id bigint NOT NULL DEFAULT nextval('users_id_seq'::regclass),
    username character varying(255) COLLATE pg_catalog."default" NOT NULL,
    password character varying(255) COLLATE pg_catalog."default" NOT NULL,
    firstname character varying(255) COLLATE pg_catalog."default",
    middlename character varying(255) COLLATE pg_catalog."default",
    lastname character varying(255) COLLATE pg_catalog."default",
    is_active boolean NOT NULL DEFAULT true,
    group_id integer,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_username_key UNIQUE (username),
    CONSTRAINT users_group_id_fkey FOREIGN KEY (group_id)
    REFERENCES public.groups (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    )

-- GROUPS (классы)
CREATE TABLE groups (
                        id SERIAL PRIMARY KEY,
                        name TEXT NOT NULL
);

ALTER TABLE users
    ADD COLUMN group_id INT REFERENCES groups(id);

CREATE TABLE subjects (
                          id SERIAL PRIMARY KEY,
                          name TEXT NOT NULL
);

-- Учитель ↔ предмет (многие ко многим)
CREATE TABLE IF NOT EXISTS teacher_subjects (
    teacher_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject_id INT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    PRIMARY KEY (teacher_id, subject_id)
);

CREATE TABLE schedule (
                          id SERIAL PRIMARY KEY,
                          group_id INT NOT NULL REFERENCES groups(id),
                          subject_id INT NOT NULL REFERENCES subjects(id),
                          teacher_id BIGINT NOT NULL REFERENCES users(id),
                          date DATE NOT NULL,
                          lesson_number INT NOT NULL
);

CREATE TABLE grades (
                        id SERIAL PRIMARY KEY,
                        student_id INT NOT NULL REFERENCES users(id),
                        schedule_id INT NOT NULL REFERENCES schedule(id),
                        value INT NOT NULL CHECK (value BETWEEN 1 AND 5),
                        comment TEXT,
                        UNIQUE (student_id, schedule_id)
);