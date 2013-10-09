/* OpenMark Authoring Tool (GEPEQ)
 * Copyright (C) 2013 UNED
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

--
-- PostgreSQL database dump
--

-- Started on 2013-07-04 14:57:04

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

ALTER TABLE ONLY public.users DROP CONSTRAINT usuarios_tipo;
ALTER TABLE ONLY public.usertypepermissions DROP CONSTRAINT usertypepermissions_usertype;
ALTER TABLE ONLY public.usertypepermissions DROP CONSTRAINT usertypepermissions_permission;
ALTER TABLE ONLY public.userpermissions DROP CONSTRAINT userpermissions_user;
ALTER TABLE ONLY public.userpermissions DROP CONSTRAINT userpermissions_permission;
ALTER TABLE ONLY public.questions_truefalse DROP CONSTRAINT truefalse_question;
ALTER TABLE ONLY public.testusers DROP CONSTRAINT testusers_user;
ALTER TABLE ONLY public.testusers DROP CONSTRAINT testusers_test;
ALTER TABLE ONLY public.tests DROP CONSTRAINT tests_scoretype;
ALTER TABLE ONLY public.tests DROP CONSTRAINT tests_redoquestionvalue;
ALTER TABLE ONLY public.tests DROP CONSTRAINT tests_navlocation;
ALTER TABLE ONLY public.tests DROP CONSTRAINT tests_modifiedby;
ALTER TABLE ONLY public.tests DROP CONSTRAINT tests_createdby;
ALTER TABLE ONLY public.tests DROP CONSTRAINT tests_category;
ALTER TABLE ONLY public.tests DROP CONSTRAINT tests_assessement;
ALTER TABLE ONLY public.testfeedbacks DROP CONSTRAINT testfeedbacks_unit;
ALTER TABLE ONLY public.testfeedbacks DROP CONSTRAINT testfeedbacks_test;
ALTER TABLE ONLY public.testfeedbacks DROP CONSTRAINT testfeedbacks_section;
ALTER TABLE ONLY public.supportcontacts DROP CONSTRAINT supportcontacts_test;
ALTER TABLE ONLY public.supportcontacts DROP CONSTRAINT supportcontacts_addresstypes;
ALTER TABLE ONLY public.sections DROP CONSTRAINT sections_test;
ALTER TABLE ONLY public.sections_questions DROP CONSTRAINT sections_question;
ALTER TABLE ONLY public.resources DROP CONSTRAINT resources_user;
ALTER TABLE ONLY public.resources DROP CONSTRAINT resources_copyright;
ALTER TABLE ONLY public.resources DROP CONSTRAINT resources_category;
ALTER TABLE ONLY public.sections_questions DROP CONSTRAINT questions_section;
ALTER TABLE ONLY public.questionresources DROP CONSTRAINT questionresources_resource;
ALTER TABLE ONLY public.questionresources DROP CONSTRAINT questionresources_question;
ALTER TABLE ONLY public.questions DROP CONSTRAINT question_resource;
ALTER TABLE ONLY public.questions DROP CONSTRAINT question_pass_feedback_resource;
ALTER TABLE ONLY public.questions DROP CONSTRAINT question_incorrect_feedback_resource;
ALTER TABLE ONLY public.questions DROP CONSTRAINT question_final_feedback_resource;
ALTER TABLE ONLY public.questions DROP CONSTRAINT question_correct_feedback_resource;
ALTER TABLE ONLY public.questions DROP CONSTRAINT pki_modifiedby;
ALTER TABLE ONLY public.questions DROP CONSTRAINT pki_createdby;
ALTER TABLE ONLY public.permissions DROP CONSTRAINT permissions_permissiontype;
ALTER TABLE ONLY public.categories DROP CONSTRAINT parent_category;
ALTER TABLE ONLY public.questions_omxml DROP CONSTRAINT omxml_question;
ALTER TABLE ONLY public.questions_multichoice DROP CONSTRAINT multichoice_question;
ALTER TABLE ONLY public.answers DROP CONSTRAINT id_question_pki;
ALTER TABLE ONLY public.questions DROP CONSTRAINT id_category_pki;
ALTER TABLE ONLY public.feedbacks DROP CONSTRAINT feedback_resource;
ALTER TABLE ONLY public.feedbacks DROP CONSTRAINT feedback_question;
ALTER TABLE ONLY public.feedbacks DROP CONSTRAINT feedback_feedbacktypes;
ALTER TABLE ONLY public.evaluators DROP CONSTRAINT evaluators_tests;
ALTER TABLE ONLY public.evaluators DROP CONSTRAINT evaluators_addresstypes;
ALTER TABLE ONLY public.answers_dragdrop DROP CONSTRAINT dragdrop_right_answer;
ALTER TABLE ONLY public.questions_dragdrop DROP CONSTRAINT dragdrop_question;
ALTER TABLE ONLY public.answers_dragdrop DROP CONSTRAINT dragdrop_answer;
ALTER TABLE ONLY public.categorytypes DROP CONSTRAINT categorytypes_parent;
ALTER TABLE ONLY public.categories DROP CONSTRAINT categories_visibility;
ALTER TABLE ONLY public.categories DROP CONSTRAINT caracteristicas_usuario;
ALTER TABLE ONLY public.answers DROP CONSTRAINT answer_resource;
DROP INDEX public.id_modifiedby;
DROP INDEX public.id_createdby;
DROP INDEX public.id_category;
DROP INDEX public.fki_usuarios_tipo;
DROP INDEX public.fki_tests_test_sections;
DROP INDEX public.fki_sections_test;
DROP INDEX public.fki_sections_question;
DROP INDEX public.fki_resources_user;
DROP INDEX public.fki_question_resource;
DROP INDEX public.fki_multichoice_question;
DROP INDEX public.fki_id_question_pki;
DROP INDEX public.fki_feedback_resource;
DROP INDEX public.fki_feedback_question;
DROP INDEX public.fki_categorytypes_categorytypes;
DROP INDEX public.fki_categorias_usuario;
DROP INDEX public.fki_categorias_categorias;
DROP INDEX public.fki_answer_resource;
ALTER TABLE ONLY public.visibilities DROP CONSTRAINT visibilities_pk;
ALTER TABLE ONLY public.users DROP CONSTRAINT usuarios_pkey;
ALTER TABLE ONLY public.usertypes DROP CONSTRAINT usertypes_utype;
ALTER TABLE ONLY public.usertypepermissions DROP CONSTRAINT usertypepermissions_pk;
ALTER TABLE ONLY public.users DROP CONSTRAINT users_uoucu;
ALTER TABLE ONLY public.users DROP CONSTRAINT users_ulogin;
ALTER TABLE ONLY public.userpermissions DROP CONSTRAINT userpermissions_pk;
ALTER TABLE ONLY public.usertypes DROP CONSTRAINT tipossusuario_pkey;
ALTER TABLE ONLY public.testusers DROP CONSTRAINT testusers_pk;
ALTER TABLE ONLY public.tests DROP CONSTRAINT tests_pkey;
ALTER TABLE ONLY public.testfeedbacks DROP CONSTRAINT testfeedbacks_pk;
ALTER TABLE ONLY public.supportcontacts DROP CONSTRAINT supportcontacts_pk;
ALTER TABLE ONLY public.sections_questions DROP CONSTRAINT sections_questions_pkey;
ALTER TABLE ONLY public.sections DROP CONSTRAINT section_pkey;
ALTER TABLE ONLY public.scoreunits DROP CONSTRAINT scoreunits_pk;
ALTER TABLE ONLY public.scoretypes DROP CONSTRAINT scoretypes_pk;
ALTER TABLE ONLY public.resources DROP CONSTRAINT resources_pkey;
ALTER TABLE ONLY public.redoquestionvalues DROP CONSTRAINT redoquestionvalues_pk;
ALTER TABLE ONLY public.questions_truefalse DROP CONSTRAINT questions_truefalse_pkey;
ALTER TABLE ONLY public.questions_omxml DROP CONSTRAINT questions_omxml_pk;
ALTER TABLE ONLY public.questions_multichoice DROP CONSTRAINT questions_multichoice_pkey;
ALTER TABLE ONLY public.questions_dragdrop DROP CONSTRAINT questions_dragdrop_pk;
ALTER TABLE ONLY public.questionresources DROP CONSTRAINT questionresources_pk;
ALTER TABLE ONLY public.questions DROP CONSTRAINT question_pkey;
ALTER TABLE ONLY public.permissiontypes DROP CONSTRAINT permissiontypes_pk;
ALTER TABLE ONLY public.permissions DROP CONSTRAINT permissions_pk;
ALTER TABLE ONLY public.navlocations DROP CONSTRAINT navlocations_pk;
ALTER TABLE ONLY public.answers DROP CONSTRAINT id_pki;
ALTER TABLE ONLY public.feedbacktypes DROP CONSTRAINT feedbacktypes_pk;
ALTER TABLE ONLY public.feedbacks DROP CONSTRAINT feedbacks_pk;
ALTER TABLE ONLY public.evaluators DROP CONSTRAINT evaluators_pk;
ALTER TABLE ONLY public.copyrights DROP CONSTRAINT copyrights_pk;
ALTER TABLE ONLY public.categorytypes DROP CONSTRAINT categorytypes_pk;
ALTER TABLE ONLY public.categories DROP CONSTRAINT categorias_pkey;
ALTER TABLE ONLY public.assessements DROP CONSTRAINT assessements_pk;
ALTER TABLE ONLY public.answers_dragdrop DROP CONSTRAINT answers_dragdrop_pk;
ALTER TABLE ONLY public.addresstypes DROP CONSTRAINT addresstypes_pk;
ALTER TABLE public.visibilities ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.usertypes ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.usertypepermissions ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.users ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.userpermissions ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.testusers ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.testfeedbacks ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.supportcontacts ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.sections_questions ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.scoreunits ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.scoretypes ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.resources ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.redoquestionvalues ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.questions ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.questionresources ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.permissiontypes ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.permissions ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.navlocations ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.feedbacktypes ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.evaluators ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.copyrights ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.categorytypes ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.categories ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.assessements ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.answers ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.addresstypes ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE public.visibilities_id_seq;
DROP TABLE public.visibilities;
DROP SEQUENCE public.usertypes_id_seq;
DROP TABLE public.usertypes;
DROP SEQUENCE public.usertypepermissions_id_seq;
DROP TABLE public.usertypepermissions;
DROP SEQUENCE public.users_id_seq;
DROP TABLE public.users;
DROP SEQUENCE public.userpermissions_id_seq;
DROP TABLE public.userpermissions;
DROP SEQUENCE public.testusers_id_seq;
DROP TABLE public.testusers;
DROP SEQUENCE public.tests_id_seq;
DROP TABLE public.tests;
DROP SEQUENCE public.testfeedbacks_id_seq;
DROP TABLE public.testfeedbacks;
DROP SEQUENCE public.supportcontacts_id_seq;
DROP TABLE public.supportcontacts;
DROP SEQUENCE public.sections_questions_id_seq;
DROP TABLE public.sections_questions;
DROP TABLE public.sections;
DROP SEQUENCE public.sections_id_seq;
DROP SEQUENCE public.scoreunits_id_seq;
DROP TABLE public.scoreunits;
DROP SEQUENCE public.scoretypes_id_seq;
DROP TABLE public.scoretypes;
DROP SEQUENCE public.resources_id_seq;
DROP TABLE public.resources;
DROP SEQUENCE public.redoquestionvalues_id_seq;
DROP TABLE public.redoquestionvalues;
DROP TABLE public.questions_truefalse;
DROP TABLE public.questions_omxml;
DROP TABLE public.questions_multichoice;
DROP TABLE public.questions_dragdrop;
DROP SEQUENCE public.questionresources_id_seq;
DROP TABLE public.questionresources;
DROP SEQUENCE public.question_id_seq;
DROP TABLE public.questions;
DROP SEQUENCE public.permissiontypes_id_seq;
DROP TABLE public.permissiontypes;
DROP SEQUENCE public.permissions_id_seq;
DROP TABLE public.permissions;
DROP SEQUENCE public.navlocations_id_seq;
DROP TABLE public.navlocations;
DROP SEQUENCE public.feedbacktypes_id_seq;
DROP TABLE public.feedbacktypes;
DROP TABLE public.feedbacks;
DROP SEQUENCE public.feedbacks_id_seq;
DROP SEQUENCE public.evaluators_id_seq;
DROP TABLE public.evaluators;
DROP SEQUENCE public.copyrights_id_seq;
DROP TABLE public.copyrights;
DROP SEQUENCE public.categorytypes_id_seq;
DROP TABLE public.categorytypes;
DROP SEQUENCE public.categories_id_seq;
DROP TABLE public.categories;
DROP SEQUENCE public.assessements_id_seq;
DROP TABLE public.assessements;
DROP SEQUENCE public.answers_id_seq;
DROP TABLE public.answers_dragdrop;
DROP TABLE public.answers;
DROP SEQUENCE public.addresstypes_id_seq;
DROP TABLE public.addresstypes;
DROP PROCEDURAL LANGUAGE plpgsql;
DROP SCHEMA public;
--
-- TOC entry 3 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA public;


ALTER SCHEMA public OWNER TO postgres;

--
-- TOC entry 2195 (class 0 OID 0)
-- Dependencies: 3
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS 'standard public schema';


--
-- TOC entry 414 (class 2612 OID 16386)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

CREATE PROCEDURAL LANGUAGE plpgsql;


ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO postgres;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1660 (class 1259 OID 39063)
-- Dependencies: 3
-- Name: addresstypes; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE addresstypes (
    id bigint NOT NULL,
    type character varying(30) NOT NULL,
    subtype character varying(30) NOT NULL
);


ALTER TABLE public.addresstypes OWNER TO postgres;

--
-- TOC entry 1659 (class 1259 OID 39061)
-- Dependencies: 1660 3
-- Name: addresstypes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE addresstypes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.addresstypes_id_seq OWNER TO postgres;

--
-- TOC entry 2197 (class 0 OID 0)
-- Dependencies: 1659
-- Name: addresstypes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE addresstypes_id_seq OWNED BY addresstypes.id;


--
-- TOC entry 1601 (class 1259 OID 17351)
-- Dependencies: 1941 1942 1943 1945 1946 3
-- Name: answers; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE answers (
    id bigint NOT NULL,
    id_question bigint,
    text text,
    correct boolean DEFAULT false,
    id_resource bigint,
    fixed boolean DEFAULT false,
    "position" integer DEFAULT 0 NOT NULL,
    name character varying(15),
    resource_width integer DEFAULT (-1) NOT NULL,
    resource_height integer DEFAULT (-1) NOT NULL
);


ALTER TABLE public.answers OWNER TO postgres;

--
-- TOC entry 1642 (class 1259 OID 19525)
-- Dependencies: 2033 3
-- Name: answers_dragdrop; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE answers_dragdrop (
    id_answer bigint NOT NULL,
    draggable boolean NOT NULL,
    dragdrop_group integer DEFAULT 1 NOT NULL,
    id_right bigint
);


ALTER TABLE public.answers_dragdrop OWNER TO postgres;

--
-- TOC entry 1602 (class 1259 OID 17360)
-- Dependencies: 1601 3
-- Name: answers_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE answers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.answers_id_seq OWNER TO postgres;

--
-- TOC entry 2198 (class 0 OID 0)
-- Dependencies: 1602
-- Name: answers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE answers_id_seq OWNED BY answers.id;


--
-- TOC entry 1654 (class 1259 OID 37738)
-- Dependencies: 3
-- Name: assessements; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE assessements (
    id bigint NOT NULL,
    type character varying(30) NOT NULL
);


ALTER TABLE public.assessements OWNER TO postgres;

--
-- TOC entry 1653 (class 1259 OID 37736)
-- Dependencies: 1654 3
-- Name: assessements_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE assessements_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.assessements_id_seq OWNER TO postgres;

--
-- TOC entry 2199 (class 0 OID 0)
-- Dependencies: 1653
-- Name: assessements_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE assessements_id_seq OWNED BY assessements.id;


--
-- TOC entry 1603 (class 1259 OID 17362)
-- Dependencies: 1947 3
-- Name: categories; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE categories (
    name character varying(30) NOT NULL,
    id_user bigint,
    id bigint NOT NULL,
    id_parent bigint,
    description text,
    default_category boolean DEFAULT false NOT NULL,
    id_type bigint NOT NULL,
    id_visibility bigint NOT NULL
);


ALTER TABLE public.categories OWNER TO postgres;

--
-- TOC entry 1604 (class 1259 OID 17366)
-- Dependencies: 3 1603
-- Name: categories_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.categories_id_seq OWNER TO postgres;

--
-- TOC entry 2200 (class 0 OID 0)
-- Dependencies: 1604
-- Name: categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE categories_id_seq OWNED BY categories.id;


--
-- TOC entry 1638 (class 1259 OID 19451)
-- Dependencies: 3
-- Name: categorytypes; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE categorytypes (
    id bigint NOT NULL,
    type character varying(30),
    icon character varying(100),
    id_parent bigint
);


ALTER TABLE public.categorytypes OWNER TO postgres;

--
-- TOC entry 1637 (class 1259 OID 19449)
-- Dependencies: 1638 3
-- Name: categorytypes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE categorytypes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.categorytypes_id_seq OWNER TO postgres;

--
-- TOC entry 2201 (class 0 OID 0)
-- Dependencies: 1637
-- Name: categorytypes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE categorytypes_id_seq OWNED BY categorytypes.id;


--
-- TOC entry 1640 (class 1259 OID 19485)
-- Dependencies: 3
-- Name: copyrights; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE copyrights (
    id bigint NOT NULL,
    copyright character varying(100)
);


ALTER TABLE public.copyrights OWNER TO postgres;

--
-- TOC entry 1639 (class 1259 OID 19483)
-- Dependencies: 1640 3
-- Name: copyrights_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE copyrights_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.copyrights_id_seq OWNER TO postgres;

--
-- TOC entry 2202 (class 0 OID 0)
-- Dependencies: 1639
-- Name: copyrights_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE copyrights_id_seq OWNED BY copyrights.id;


--
-- TOC entry 1656 (class 1259 OID 38025)
-- Dependencies: 3
-- Name: evaluators; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE evaluators (
    id bigint NOT NULL,
    id_test bigint NOT NULL,
    evaluator text,
    id_addresstype bigint NOT NULL,
    filtervalue text
);


ALTER TABLE public.evaluators OWNER TO postgres;

--
-- TOC entry 1655 (class 1259 OID 38023)
-- Dependencies: 3 1656
-- Name: evaluators_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE evaluators_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.evaluators_id_seq OWNER TO postgres;

--
-- TOC entry 2203 (class 0 OID 0)
-- Dependencies: 1655
-- Name: evaluators_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE evaluators_id_seq OWNED BY evaluators.id;


--
-- TOC entry 1622 (class 1259 OID 17959)
-- Dependencies: 3
-- Name: feedbacks_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE feedbacks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.feedbacks_id_seq OWNER TO postgres;

--
-- TOC entry 1621 (class 1259 OID 17953)
-- Dependencies: 1994 1995 1996 1997 1998 1999 2000 2001 2002 2003 2004 2005 2006 2007 2008 2009 2010 2011 2012 2013 2014 3
-- Name: feedbacks; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE feedbacks (
    id bigint DEFAULT nextval('feedbacks_id_seq'::regclass) NOT NULL,
    id_question bigint NOT NULL,
    text text,
    id_resource bigint,
    test text DEFAULT true NOT NULL,
    answer text,
    attemptsmin integer DEFAULT 0 NOT NULL,
    attemptsmax integer DEFAULT 2147483647 NOT NULL,
    selectedanswersmin integer DEFAULT 0 NOT NULL,
    selectedanswersmax integer DEFAULT 2147483647 NOT NULL,
    selectedrightanswersmin integer DEFAULT 0 NOT NULL,
    selectedrightanswersmax integer DEFAULT 2147483647 NOT NULL,
    selectedwronganswersmin integer DEFAULT 0 NOT NULL,
    selectedwronganswersmax integer DEFAULT 2147483647 NOT NULL,
    unselectedanswersmin integer DEFAULT 0 NOT NULL,
    unselectedanswersmax integer DEFAULT 2147483647 NOT NULL,
    unselectedrightanswersmin integer DEFAULT 0 NOT NULL,
    unselectedrightanswersmax integer DEFAULT 2147483647 NOT NULL,
    unselectedwronganswersmin integer DEFAULT 0 NOT NULL,
    unselectedwronganswersmax integer DEFAULT 2147483647 NOT NULL,
    rightdistancemin integer DEFAULT 0 NOT NULL,
    rightdistancemax integer DEFAULT 2147483647 NOT NULL,
    "position" integer DEFAULT 0 NOT NULL,
    resource_width integer DEFAULT (-1) NOT NULL,
    resource_height integer DEFAULT (-1) NOT NULL,
    id_type bigint
);


ALTER TABLE public.feedbacks OWNER TO postgres;

--
-- TOC entry 1624 (class 1259 OID 18278)
-- Dependencies: 3
-- Name: feedbacktypes; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE feedbacktypes (
    id bigint NOT NULL,
    type character varying(30) NOT NULL
);


ALTER TABLE public.feedbacktypes OWNER TO postgres;

--
-- TOC entry 1623 (class 1259 OID 18276)
-- Dependencies: 3 1624
-- Name: feedbacktypes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE feedbacktypes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.feedbacktypes_id_seq OWNER TO postgres;

--
-- TOC entry 2204 (class 0 OID 0)
-- Dependencies: 1623
-- Name: feedbacktypes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE feedbacktypes_id_seq OWNED BY feedbacktypes.id;


--
-- TOC entry 1626 (class 1259 OID 18606)
-- Dependencies: 3
-- Name: navlocations; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE navlocations (
    id bigint NOT NULL,
    location character varying(30) NOT NULL
);


ALTER TABLE public.navlocations OWNER TO postgres;

--
-- TOC entry 1625 (class 1259 OID 18604)
-- Dependencies: 1626 3
-- Name: navlocations_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE navlocations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.navlocations_id_seq OWNER TO postgres;

--
-- TOC entry 2205 (class 0 OID 0)
-- Dependencies: 1625
-- Name: navlocations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE navlocations_id_seq OWNED BY navlocations.id;


--
-- TOC entry 1646 (class 1259 OID 28021)
-- Dependencies: 3
-- Name: permissions; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE permissions (
    id bigint NOT NULL,
    name character varying(100) NOT NULL,
    id_type bigint NOT NULL,
    default_value text NOT NULL
);


ALTER TABLE public.permissions OWNER TO postgres;

--
-- TOC entry 1645 (class 1259 OID 28019)
-- Dependencies: 3 1646
-- Name: permissions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE permissions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.permissions_id_seq OWNER TO postgres;

--
-- TOC entry 2206 (class 0 OID 0)
-- Dependencies: 1645
-- Name: permissions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE permissions_id_seq OWNED BY permissions.id;


--
-- TOC entry 1644 (class 1259 OID 28013)
-- Dependencies: 3
-- Name: permissiontypes; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE permissiontypes (
    id bigint NOT NULL,
    type character varying(100) NOT NULL
);


ALTER TABLE public.permissiontypes OWNER TO postgres;

--
-- TOC entry 1643 (class 1259 OID 28011)
-- Dependencies: 1644 3
-- Name: permissiontypes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE permissiontypes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.permissiontypes_id_seq OWNER TO postgres;

--
-- TOC entry 2207 (class 0 OID 0)
-- Dependencies: 1643
-- Name: permissiontypes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE permissiontypes_id_seq OWNED BY permissiontypes.id;


--
-- TOC entry 1605 (class 1259 OID 17368)
-- Dependencies: 1949 1951 1952 1953 1954 1955 1956 1957 1958 1959 1960 1961 3
-- Name: questions; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE questions (
    id bigint NOT NULL,
    id_category bigint NOT NULL,
    name character varying(50) NOT NULL,
    question_text text,
    version character(50),
    id_createdby bigint NOT NULL,
    id_modifiedby bigint,
    correct_feedback text,
    incorrect_feedback text,
    id_resource bigint,
    level character varying(10) DEFAULT 'NORMAL'::character varying NOT NULL,
    timecreated timestamp with time zone,
    timemodified timestamp with time zone,
    timebuild timestamp with time zone,
    timedeploy timestamp with time zone,
    still_feedback text,
    pass_feedback text,
    answer_feedback text,
    resource_width integer DEFAULT (-1) NOT NULL,
    resource_height integer DEFAULT (-1) NOT NULL,
    type character varying(25) NOT NULL,
    timepublished timestamp with time zone,
    id_correct_feedback_resource bigint,
    id_incorrect_feedback_resource bigint,
    id_pass_feedback_resource bigint,
    id_final_feedback_resource bigint,
    correct_feedback_resource_width integer DEFAULT (-1) NOT NULL,
    correct_feedback_resource_height integer DEFAULT (-1) NOT NULL,
    incorrect_feedback_resource_width integer DEFAULT (-1) NOT NULL,
    incorrect_feedback_resource_height integer DEFAULT (-1) NOT NULL,
    pass_feedback_resource_width integer DEFAULT (-1) NOT NULL,
    pass_feedback_resource_height integer DEFAULT (-1) NOT NULL,
    final_feedback_resource_width integer DEFAULT (-1) NOT NULL,
    final_feedback_resource_height integer DEFAULT (-1) NOT NULL,
    display_equations boolean DEFAULT false NOT NULL
);


ALTER TABLE public.questions OWNER TO postgres;

--
-- TOC entry 1606 (class 1259 OID 17375)
-- Dependencies: 3 1605
-- Name: question_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE question_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.question_id_seq OWNER TO postgres;

--
-- TOC entry 2208 (class 0 OID 0)
-- Dependencies: 1606
-- Name: question_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE question_id_seq OWNED BY questions.id;


--
-- TOC entry 1663 (class 1259 OID 40877)
-- Dependencies: 2046 2047 3
-- Name: questionresources; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE questionresources (
    id bigint NOT NULL,
    id_question bigint NOT NULL,
    id_resource bigint NOT NULL,
    "position" integer NOT NULL,
    name character varying(15),
    width integer DEFAULT (-1) NOT NULL,
    height integer DEFAULT (-1) NOT NULL
);


ALTER TABLE public.questionresources OWNER TO postgres;

--
-- TOC entry 1662 (class 1259 OID 40875)
-- Dependencies: 3 1663
-- Name: questionresources_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE questionresources_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.questionresources_id_seq OWNER TO postgres;

--
-- TOC entry 2209 (class 0 OID 0)
-- Dependencies: 1662
-- Name: questionresources_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE questionresources_id_seq OWNED BY questionresources.id;


--
-- TOC entry 1641 (class 1259 OID 19509)
-- Dependencies: 2027 2028 2029 2030 2031 2032 3
-- Name: questions_dragdrop; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE questions_dragdrop (
    id_question bigint NOT NULL,
    infinite boolean DEFAULT false NOT NULL,
    forceborders boolean DEFAULT false NOT NULL,
    shuffle_drags boolean DEFAULT false NOT NULL,
    shuffle_drops boolean DEFAULT false NOT NULL,
    clustered_drags boolean DEFAULT true NOT NULL,
    clustered_drops boolean DEFAULT true NOT NULL
);


ALTER TABLE public.questions_dragdrop OWNER TO postgres;

--
-- TOC entry 1607 (class 1259 OID 17377)
-- Dependencies: 1962 1963 3
-- Name: questions_multichoice; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE questions_multichoice (
    id_question bigint NOT NULL,
    single boolean DEFAULT true NOT NULL,
    shuffle boolean DEFAULT false NOT NULL
);


ALTER TABLE public.questions_multichoice OWNER TO postgres;

--
-- TOC entry 1661 (class 1259 OID 40728)
-- Dependencies: 3
-- Name: questions_omxml; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE questions_omxml (
    id_question bigint NOT NULL,
    xml_content text
);


ALTER TABLE public.questions_omxml OWNER TO postgres;

--
-- TOC entry 1608 (class 1259 OID 17382)
-- Dependencies: 1964 3
-- Name: questions_truefalse; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE questions_truefalse (
    id_question bigint NOT NULL,
    correct_answer boolean DEFAULT true NOT NULL,
    true_text character varying(25),
    false_text character varying(25)
);


ALTER TABLE public.questions_truefalse OWNER TO postgres;

--
-- TOC entry 1628 (class 1259 OID 18614)
-- Dependencies: 3
-- Name: redoquestionvalues; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE redoquestionvalues (
    id bigint NOT NULL,
    value character varying(30) NOT NULL
);


ALTER TABLE public.redoquestionvalues OWNER TO postgres;

--
-- TOC entry 1627 (class 1259 OID 18612)
-- Dependencies: 1628 3
-- Name: redoquestionvalues_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE redoquestionvalues_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.redoquestionvalues_id_seq OWNER TO postgres;

--
-- TOC entry 2210 (class 0 OID 0)
-- Dependencies: 1627
-- Name: redoquestionvalues_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE redoquestionvalues_id_seq OWNED BY redoquestionvalues.id;


--
-- TOC entry 1609 (class 1259 OID 17386)
-- Dependencies: 3
-- Name: resources; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE resources (
    id integer NOT NULL,
    name character varying(25) NOT NULL,
    description text,
    id_user bigint,
    file_name text,
    mime_type character varying(30),
    id_category bigint NOT NULL,
    id_copyright bigint NOT NULL
);


ALTER TABLE public.resources OWNER TO postgres;

--
-- TOC entry 1610 (class 1259 OID 17389)
-- Dependencies: 1609 3
-- Name: resources_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE resources_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.resources_id_seq OWNER TO postgres;

--
-- TOC entry 2211 (class 0 OID 0)
-- Dependencies: 1610
-- Name: resources_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE resources_id_seq OWNED BY resources.id;


--
-- TOC entry 1658 (class 1259 OID 38124)
-- Dependencies: 3
-- Name: scoretypes; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE scoretypes (
    id bigint NOT NULL,
    type character varying(30)
);


ALTER TABLE public.scoretypes OWNER TO postgres;

--
-- TOC entry 1657 (class 1259 OID 38122)
-- Dependencies: 1658 3
-- Name: scoretypes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE scoretypes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.scoretypes_id_seq OWNER TO postgres;

--
-- TOC entry 2212 (class 0 OID 0)
-- Dependencies: 1657
-- Name: scoretypes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE scoretypes_id_seq OWNED BY scoretypes.id;


--
-- TOC entry 1634 (class 1259 OID 18666)
-- Dependencies: 3
-- Name: scoreunits; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE scoreunits (
    id bigint NOT NULL,
    unit character varying(30)
);


ALTER TABLE public.scoreunits OWNER TO postgres;

--
-- TOC entry 1633 (class 1259 OID 18664)
-- Dependencies: 1634 3
-- Name: scoreunits_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE scoreunits_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.scoreunits_id_seq OWNER TO postgres;

--
-- TOC entry 2213 (class 0 OID 0)
-- Dependencies: 1633
-- Name: scoreunits_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE scoreunits_id_seq OWNED BY scoreunits.id;


--
-- TOC entry 1611 (class 1259 OID 17391)
-- Dependencies: 3
-- Name: sections_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE sections_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.sections_id_seq OWNER TO postgres;

--
-- TOC entry 1612 (class 1259 OID 17393)
-- Dependencies: 1966 1967 1968 1969 1970 1971 3
-- Name: sections; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE sections (
    id bigint DEFAULT nextval('sections_id_seq'::regclass) NOT NULL,
    id_test bigint NOT NULL,
    section_order integer DEFAULT 1 NOT NULL,
    random_quantity integer DEFAULT 1 NOT NULL,
    shuffle boolean DEFAULT false NOT NULL,
    random boolean DEFAULT false NOT NULL,
    name character varying(15),
    title character varying(25),
    weight integer DEFAULT 1 NOT NULL
);


ALTER TABLE public.sections OWNER TO postgres;

--
-- TOC entry 1613 (class 1259 OID 17404)
-- Dependencies: 1972 1974 3
-- Name: sections_questions; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE sections_questions (
    id_question bigint,
    id_section bigint NOT NULL,
    order_number integer DEFAULT 0 NOT NULL,
    id integer NOT NULL,
    weight integer DEFAULT 1 NOT NULL
);


ALTER TABLE public.sections_questions OWNER TO postgres;

--
-- TOC entry 1614 (class 1259 OID 17408)
-- Dependencies: 3 1613
-- Name: sections_questions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE sections_questions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.sections_questions_id_seq OWNER TO postgres;

--
-- TOC entry 2214 (class 0 OID 0)
-- Dependencies: 1614
-- Name: sections_questions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE sections_questions_id_seq OWNED BY sections_questions.id;


--
-- TOC entry 1630 (class 1259 OID 18642)
-- Dependencies: 3
-- Name: supportcontacts; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE supportcontacts (
    id bigint NOT NULL,
    id_test bigint NOT NULL,
    supportcontact text,
    id_addresstype bigint NOT NULL,
    filtervalue text
);


ALTER TABLE public.supportcontacts OWNER TO postgres;

--
-- TOC entry 1629 (class 1259 OID 18640)
-- Dependencies: 3 1630
-- Name: supportcontacts_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE supportcontacts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.supportcontacts_id_seq OWNER TO postgres;

--
-- TOC entry 2215 (class 0 OID 0)
-- Dependencies: 1629
-- Name: supportcontacts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE supportcontacts_id_seq OWNED BY supportcontacts.id;


--
-- TOC entry 1632 (class 1259 OID 18655)
-- Dependencies: 2020 3
-- Name: testfeedbacks; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE testfeedbacks (
    id bigint NOT NULL,
    text text,
    id_section bigint,
    id_unit bigint,
    minvalue integer,
    maxvalue integer,
    id_test bigint NOT NULL,
    "position" integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.testfeedbacks OWNER TO postgres;

--
-- TOC entry 1631 (class 1259 OID 18653)
-- Dependencies: 3 1632
-- Name: testfeedbacks_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE testfeedbacks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.testfeedbacks_id_seq OWNER TO postgres;

--
-- TOC entry 2216 (class 0 OID 0)
-- Dependencies: 1631
-- Name: testfeedbacks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE testfeedbacks_id_seq OWNED BY testfeedbacks.id;


--
-- TOC entry 1615 (class 1259 OID 17410)
-- Dependencies: 1975 1976 1977 1978 1979 1980 1981 1982 1983 1984 1985 1986 1987 1988 1989 1990 3
-- Name: tests; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE tests (
    id bigint DEFAULT nextval('resources_id_seq'::regclass) NOT NULL,
    name character varying(50) NOT NULL,
    description text,
    title character varying(25),
    all_users_allowed boolean DEFAULT true NOT NULL,
    allow_admin_reports boolean DEFAULT true NOT NULL,
    id_createdby bigint NOT NULL,
    id_modifiedby bigint,
    timecreated timestamp with time zone,
    timemodified timestamp with time zone,
    start_date timestamp with time zone,
    close_date timestamp with time zone,
    warning_date timestamp with time zone,
    feedback_date timestamp with time zone,
    freesummary boolean DEFAULT false NOT NULL,
    freestop boolean DEFAULT true NOT NULL,
    summaryquestions boolean DEFAULT false NOT NULL,
    summaryscores boolean DEFAULT false NOT NULL,
    summaryattempts boolean DEFAULT true NOT NULL,
    navigation boolean DEFAULT true NOT NULL,
    id_navlocation bigint,
    id_redoquestion bigint,
    redotest boolean DEFAULT true NOT NULL,
    presentation_title character varying(25),
    presentation text,
    preliminarysummary_title character varying(25),
    preliminarysummary_button character varying(50),
    preliminarysummary text,
    feedback_displaysummary boolean DEFAULT true NOT NULL,
    feedback_displaysummarymarks boolean DEFAULT false NOT NULL,
    feedback_displaysummaryattempts boolean DEFAULT true NOT NULL,
    feedback_summaryprevious text,
    feedback_displayscores boolean DEFAULT true NOT NULL,
    feedback_displayscoresmarks boolean DEFAULT false NOT NULL,
    feedback_displayscorespercentages boolean DEFAULT true NOT NULL,
    feedback_scoresprevious text,
    feedback_advancedprevious text,
    feedback_advancednext text,
    id_category bigint NOT NULL,
    timetestdeploy timestamp with time zone,
    timedeploydeploy timestamp with time zone,
    id_assessement bigint,
    id_scoretype bigint,
    usergroups text,
    admingroups text
);


ALTER TABLE public.tests OWNER TO postgres;

--
-- TOC entry 1616 (class 1259 OID 17414)
-- Dependencies: 3
-- Name: tests_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE tests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.tests_id_seq OWNER TO postgres;

--
-- TOC entry 1651 (class 1259 OID 37157)
-- Dependencies: 2039 2040 3
-- Name: testusers; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE testusers (
    id bigint NOT NULL,
    id_test bigint NOT NULL,
    id_user bigint NOT NULL,
    om_user boolean DEFAULT true NOT NULL,
    om_admin boolean DEFAULT false NOT NULL
);


ALTER TABLE public.testusers OWNER TO postgres;

--
-- TOC entry 1652 (class 1259 OID 37160)
-- Dependencies: 3 1651
-- Name: testusers_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE testusers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.testusers_id_seq OWNER TO postgres;

--
-- TOC entry 2217 (class 0 OID 0)
-- Dependencies: 1652
-- Name: testusers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE testusers_id_seq OWNED BY testusers.id;


--
-- TOC entry 1650 (class 1259 OID 28058)
-- Dependencies: 3
-- Name: userpermissions; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE userpermissions (
    id bigint NOT NULL,
    id_user bigint NOT NULL,
    id_permission bigint NOT NULL,
    value text NOT NULL
);


ALTER TABLE public.userpermissions OWNER TO postgres;

--
-- TOC entry 1649 (class 1259 OID 28056)
-- Dependencies: 3 1650
-- Name: userpermissions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE userpermissions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.userpermissions_id_seq OWNER TO postgres;

--
-- TOC entry 2218 (class 0 OID 0)
-- Dependencies: 1649
-- Name: userpermissions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE userpermissions_id_seq OWNED BY userpermissions.id;


--
-- TOC entry 1617 (class 1259 OID 17416)
-- Dependencies: 1992 3
-- Name: users; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE users (
    login character varying(15) NOT NULL,
    password character varying(100) NOT NULL,
    name character varying(50),
    surname character varying(80),
    id bigint NOT NULL,
    id_type bigint,
    nick character varying(15) NOT NULL,
    gepeq_user boolean DEFAULT true NOT NULL,
    oucu character varying(8) NOT NULL,
    groups text
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 1618 (class 1259 OID 17422)
-- Dependencies: 3 1617
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.users_id_seq OWNER TO postgres;

--
-- TOC entry 2219 (class 0 OID 0)
-- Dependencies: 1618
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE users_id_seq OWNED BY users.id;


--
-- TOC entry 1648 (class 1259 OID 28037)
-- Dependencies: 3
-- Name: usertypepermissions; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE usertypepermissions (
    id bigint NOT NULL,
    id_usertype bigint NOT NULL,
    id_permission bigint NOT NULL,
    value text NOT NULL
);


ALTER TABLE public.usertypepermissions OWNER TO postgres;

--
-- TOC entry 1647 (class 1259 OID 28035)
-- Dependencies: 3 1648
-- Name: usertypepermissions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE usertypepermissions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.usertypepermissions_id_seq OWNER TO postgres;

--
-- TOC entry 2220 (class 0 OID 0)
-- Dependencies: 1647
-- Name: usertypepermissions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE usertypepermissions_id_seq OWNED BY usertypepermissions.id;


--
-- TOC entry 1619 (class 1259 OID 17424)
-- Dependencies: 3
-- Name: usertypes; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE usertypes (
    type character varying(25) NOT NULL,
    id bigint NOT NULL,
    description text
);


ALTER TABLE public.usertypes OWNER TO postgres;

--
-- TOC entry 1620 (class 1259 OID 17427)
-- Dependencies: 3 1619
-- Name: usertypes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE usertypes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.usertypes_id_seq OWNER TO postgres;

--
-- TOC entry 2221 (class 0 OID 0)
-- Dependencies: 1620
-- Name: usertypes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE usertypes_id_seq OWNED BY usertypes.id;


--
-- TOC entry 1636 (class 1259 OID 19129)
-- Dependencies: 2023 2024 3
-- Name: visibilities; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE visibilities (
    id bigint NOT NULL,
    visibility character varying(30),
    global boolean DEFAULT false NOT NULL,
    level integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.visibilities OWNER TO postgres;

--
-- TOC entry 1635 (class 1259 OID 19127)
-- Dependencies: 1636 3
-- Name: visibilities_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE visibilities_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.visibilities_id_seq OWNER TO postgres;

--
-- TOC entry 2222 (class 0 OID 0)
-- Dependencies: 1635
-- Name: visibilities_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE visibilities_id_seq OWNED BY visibilities.id;


--
-- TOC entry 2044 (class 2604 OID 39066)
-- Dependencies: 1659 1660 1660
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE addresstypes ALTER COLUMN id SET DEFAULT nextval('addresstypes_id_seq'::regclass);


--
-- TOC entry 1944 (class 2604 OID 17429)
-- Dependencies: 1602 1601
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE answers ALTER COLUMN id SET DEFAULT nextval('answers_id_seq'::regclass);


--
-- TOC entry 2041 (class 2604 OID 37741)
-- Dependencies: 1654 1653 1654
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE assessements ALTER COLUMN id SET DEFAULT nextval('assessements_id_seq'::regclass);


--
-- TOC entry 1948 (class 2604 OID 17430)
-- Dependencies: 1604 1603
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE categories ALTER COLUMN id SET DEFAULT nextval('categories_id_seq'::regclass);


--
-- TOC entry 2025 (class 2604 OID 19454)
-- Dependencies: 1638 1637 1638
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE categorytypes ALTER COLUMN id SET DEFAULT nextval('categorytypes_id_seq'::regclass);


--
-- TOC entry 2026 (class 2604 OID 19488)
-- Dependencies: 1639 1640 1640
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE copyrights ALTER COLUMN id SET DEFAULT nextval('copyrights_id_seq'::regclass);


--
-- TOC entry 2042 (class 2604 OID 38028)
-- Dependencies: 1655 1656 1656
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE evaluators ALTER COLUMN id SET DEFAULT nextval('evaluators_id_seq'::regclass);


--
-- TOC entry 2015 (class 2604 OID 18281)
-- Dependencies: 1624 1623 1624
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE feedbacktypes ALTER COLUMN id SET DEFAULT nextval('feedbacktypes_id_seq'::regclass);


--
-- TOC entry 2016 (class 2604 OID 18609)
-- Dependencies: 1626 1625 1626
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE navlocations ALTER COLUMN id SET DEFAULT nextval('navlocations_id_seq'::regclass);


--
-- TOC entry 2035 (class 2604 OID 28024)
-- Dependencies: 1646 1645 1646
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE permissions ALTER COLUMN id SET DEFAULT nextval('permissions_id_seq'::regclass);


--
-- TOC entry 2034 (class 2604 OID 28016)
-- Dependencies: 1643 1644 1644
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE permissiontypes ALTER COLUMN id SET DEFAULT nextval('permissiontypes_id_seq'::regclass);


--
-- TOC entry 2045 (class 2604 OID 40880)
-- Dependencies: 1662 1663 1663
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE questionresources ALTER COLUMN id SET DEFAULT nextval('questionresources_id_seq'::regclass);


--
-- TOC entry 1950 (class 2604 OID 17431)
-- Dependencies: 1606 1605
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE questions ALTER COLUMN id SET DEFAULT nextval('question_id_seq'::regclass);


--
-- TOC entry 2017 (class 2604 OID 18617)
-- Dependencies: 1627 1628 1628
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE redoquestionvalues ALTER COLUMN id SET DEFAULT nextval('redoquestionvalues_id_seq'::regclass);


--
-- TOC entry 1965 (class 2604 OID 17432)
-- Dependencies: 1610 1609
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE resources ALTER COLUMN id SET DEFAULT nextval('resources_id_seq'::regclass);


--
-- TOC entry 2043 (class 2604 OID 38127)
-- Dependencies: 1658 1657 1658
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE scoretypes ALTER COLUMN id SET DEFAULT nextval('scoretypes_id_seq'::regclass);


--
-- TOC entry 2021 (class 2604 OID 18669)
-- Dependencies: 1634 1633 1634
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE scoreunits ALTER COLUMN id SET DEFAULT nextval('scoreunits_id_seq'::regclass);


--
-- TOC entry 1973 (class 2604 OID 17433)
-- Dependencies: 1614 1613
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE sections_questions ALTER COLUMN id SET DEFAULT nextval('sections_questions_id_seq'::regclass);


--
-- TOC entry 2018 (class 2604 OID 18645)
-- Dependencies: 1629 1630 1630
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE supportcontacts ALTER COLUMN id SET DEFAULT nextval('supportcontacts_id_seq'::regclass);


--
-- TOC entry 2019 (class 2604 OID 18658)
-- Dependencies: 1632 1631 1632
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE testfeedbacks ALTER COLUMN id SET DEFAULT nextval('testfeedbacks_id_seq'::regclass);


--
-- TOC entry 2038 (class 2604 OID 37162)
-- Dependencies: 1652 1651
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE testusers ALTER COLUMN id SET DEFAULT nextval('testusers_id_seq'::regclass);


--
-- TOC entry 2037 (class 2604 OID 28061)
-- Dependencies: 1649 1650 1650
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE userpermissions ALTER COLUMN id SET DEFAULT nextval('userpermissions_id_seq'::regclass);


--
-- TOC entry 1991 (class 2604 OID 17434)
-- Dependencies: 1618 1617
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE users ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);


--
-- TOC entry 2036 (class 2604 OID 28040)
-- Dependencies: 1648 1647 1648
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE usertypepermissions ALTER COLUMN id SET DEFAULT nextval('usertypepermissions_id_seq'::regclass);


--
-- TOC entry 1993 (class 2604 OID 17435)
-- Dependencies: 1620 1619
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE usertypes ALTER COLUMN id SET DEFAULT nextval('usertypes_id_seq'::regclass);


--
-- TOC entry 2022 (class 2604 OID 19132)
-- Dependencies: 1636 1635 1636
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE visibilities ALTER COLUMN id SET DEFAULT nextval('visibilities_id_seq'::regclass);


--
-- TOC entry 2134 (class 2606 OID 39068)
-- Dependencies: 1660 1660
-- Name: addresstypes_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY addresstypes
    ADD CONSTRAINT addresstypes_pk PRIMARY KEY (id);


--
-- TOC entry 2116 (class 2606 OID 19530)
-- Dependencies: 1642 1642
-- Name: answers_dragdrop_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY answers_dragdrop
    ADD CONSTRAINT answers_dragdrop_pk PRIMARY KEY (id_answer);


--
-- TOC entry 2128 (class 2606 OID 37743)
-- Dependencies: 1654 1654
-- Name: assessements_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY assessements
    ADD CONSTRAINT assessements_pk PRIMARY KEY (id);


--
-- TOC entry 2053 (class 2606 OID 17437)
-- Dependencies: 1603 1603
-- Name: categorias_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY categories
    ADD CONSTRAINT categorias_pkey PRIMARY KEY (id);


--
-- TOC entry 2109 (class 2606 OID 19456)
-- Dependencies: 1638 1638
-- Name: categorytypes_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY categorytypes
    ADD CONSTRAINT categorytypes_pk PRIMARY KEY (id);


--
-- TOC entry 2112 (class 2606 OID 19490)
-- Dependencies: 1640 1640
-- Name: copyrights_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY copyrights
    ADD CONSTRAINT copyrights_pk PRIMARY KEY (id);


--
-- TOC entry 2130 (class 2606 OID 38033)
-- Dependencies: 1656 1656
-- Name: evaluators_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY evaluators
    ADD CONSTRAINT evaluators_pk PRIMARY KEY (id);


--
-- TOC entry 2091 (class 2606 OID 17963)
-- Dependencies: 1621 1621
-- Name: feedbacks_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY feedbacks
    ADD CONSTRAINT feedbacks_pk PRIMARY KEY (id);


--
-- TOC entry 2095 (class 2606 OID 18283)
-- Dependencies: 1624 1624
-- Name: feedbacktypes_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY feedbacktypes
    ADD CONSTRAINT feedbacktypes_pk PRIMARY KEY (id);


--
-- TOC entry 2051 (class 2606 OID 17439)
-- Dependencies: 1601 1601
-- Name: id_pki; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY answers
    ADD CONSTRAINT id_pki PRIMARY KEY (id);


--
-- TOC entry 2097 (class 2606 OID 18611)
-- Dependencies: 1626 1626
-- Name: navlocations_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY navlocations
    ADD CONSTRAINT navlocations_pk PRIMARY KEY (id);


--
-- TOC entry 2120 (class 2606 OID 28029)
-- Dependencies: 1646 1646
-- Name: permissions_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY permissions
    ADD CONSTRAINT permissions_pk PRIMARY KEY (id);


--
-- TOC entry 2118 (class 2606 OID 28018)
-- Dependencies: 1644 1644
-- Name: permissiontypes_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY permissiontypes
    ADD CONSTRAINT permissiontypes_pk PRIMARY KEY (id);


--
-- TOC entry 2061 (class 2606 OID 17441)
-- Dependencies: 1605 1605
-- Name: question_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY questions
    ADD CONSTRAINT question_pkey PRIMARY KEY (id);


--
-- TOC entry 2138 (class 2606 OID 40884)
-- Dependencies: 1663 1663
-- Name: questionresources_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY questionresources
    ADD CONSTRAINT questionresources_pk PRIMARY KEY (id);


--
-- TOC entry 2114 (class 2606 OID 19519)
-- Dependencies: 1641 1641
-- Name: questions_dragdrop_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY questions_dragdrop
    ADD CONSTRAINT questions_dragdrop_pk PRIMARY KEY (id_question);


--
-- TOC entry 2064 (class 2606 OID 17443)
-- Dependencies: 1607 1607
-- Name: questions_multichoice_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY questions_multichoice
    ADD CONSTRAINT questions_multichoice_pkey PRIMARY KEY (id_question);


--
-- TOC entry 2136 (class 2606 OID 40735)
-- Dependencies: 1661 1661
-- Name: questions_omxml_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY questions_omxml
    ADD CONSTRAINT questions_omxml_pk PRIMARY KEY (id_question);


--
-- TOC entry 2066 (class 2606 OID 17445)
-- Dependencies: 1608 1608
-- Name: questions_truefalse_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY questions_truefalse
    ADD CONSTRAINT questions_truefalse_pkey PRIMARY KEY (id_question);


--
-- TOC entry 2099 (class 2606 OID 18619)
-- Dependencies: 1628 1628
-- Name: redoquestionvalues_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY redoquestionvalues
    ADD CONSTRAINT redoquestionvalues_pk PRIMARY KEY (id);


--
-- TOC entry 2069 (class 2606 OID 17447)
-- Dependencies: 1609 1609
-- Name: resources_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY resources
    ADD CONSTRAINT resources_pkey PRIMARY KEY (id);


--
-- TOC entry 2132 (class 2606 OID 38129)
-- Dependencies: 1658 1658
-- Name: scoretypes_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY scoretypes
    ADD CONSTRAINT scoretypes_pk PRIMARY KEY (id);


--
-- TOC entry 2105 (class 2606 OID 18671)
-- Dependencies: 1634 1634
-- Name: scoreunits_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY scoreunits
    ADD CONSTRAINT scoreunits_pk PRIMARY KEY (id);


--
-- TOC entry 2072 (class 2606 OID 17449)
-- Dependencies: 1612 1612
-- Name: section_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY sections
    ADD CONSTRAINT section_pkey PRIMARY KEY (id);


--
-- TOC entry 2076 (class 2606 OID 17451)
-- Dependencies: 1613 1613
-- Name: sections_questions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY sections_questions
    ADD CONSTRAINT sections_questions_pkey PRIMARY KEY (id);


--
-- TOC entry 2101 (class 2606 OID 18647)
-- Dependencies: 1630 1630
-- Name: supportcontacts_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY supportcontacts
    ADD CONSTRAINT supportcontacts_pk PRIMARY KEY (id);


--
-- TOC entry 2103 (class 2606 OID 18663)
-- Dependencies: 1632 1632
-- Name: testfeedbacks_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY testfeedbacks
    ADD CONSTRAINT testfeedbacks_pk PRIMARY KEY (id);


--
-- TOC entry 2078 (class 2606 OID 17453)
-- Dependencies: 1615 1615
-- Name: tests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY tests
    ADD CONSTRAINT tests_pkey PRIMARY KEY (id);


--
-- TOC entry 2126 (class 2606 OID 37175)
-- Dependencies: 1651 1651
-- Name: testusers_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY testusers
    ADD CONSTRAINT testusers_pk PRIMARY KEY (id);


--
-- TOC entry 2087 (class 2606 OID 17455)
-- Dependencies: 1619 1619
-- Name: tipossusuario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY usertypes
    ADD CONSTRAINT tipossusuario_pkey PRIMARY KEY (id);


--
-- TOC entry 2124 (class 2606 OID 28066)
-- Dependencies: 1650 1650
-- Name: userpermissions_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY userpermissions
    ADD CONSTRAINT userpermissions_pk PRIMARY KEY (id);


--
-- TOC entry 2081 (class 2606 OID 36705)
-- Dependencies: 1617 1617
-- Name: users_ulogin; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_ulogin UNIQUE (login);


--
-- TOC entry 2083 (class 2606 OID 36747)
-- Dependencies: 1617 1617
-- Name: users_uoucu; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_uoucu UNIQUE (oucu);


--
-- TOC entry 2122 (class 2606 OID 28045)
-- Dependencies: 1648 1648
-- Name: usertypepermissions_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY usertypepermissions
    ADD CONSTRAINT usertypepermissions_pk PRIMARY KEY (id);


--
-- TOC entry 2089 (class 2606 OID 37447)
-- Dependencies: 1619 1619
-- Name: usertypes_utype; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY usertypes
    ADD CONSTRAINT usertypes_utype UNIQUE (type);


--
-- TOC entry 2085 (class 2606 OID 17457)
-- Dependencies: 1617 1617
-- Name: usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- TOC entry 2107 (class 2606 OID 19136)
-- Dependencies: 1636 1636
-- Name: visibilities_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY visibilities
    ADD CONSTRAINT visibilities_pk PRIMARY KEY (id);


--
-- TOC entry 2048 (class 1259 OID 17458)
-- Dependencies: 1601
-- Name: fki_answer_resource; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fki_answer_resource ON answers USING btree (id_resource);


--
-- TOC entry 2054 (class 1259 OID 17459)
-- Dependencies: 1603
-- Name: fki_categorias_categorias; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fki_categorias_categorias ON categories USING btree (id_parent);


--
-- TOC entry 2055 (class 1259 OID 17460)
-- Dependencies: 1603
-- Name: fki_categorias_usuario; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fki_categorias_usuario ON categories USING btree (id_user);


--
-- TOC entry 2110 (class 1259 OID 19462)
-- Dependencies: 1638
-- Name: fki_categorytypes_categorytypes; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fki_categorytypes_categorytypes ON categorytypes USING btree (id_parent);


--
-- TOC entry 2092 (class 1259 OID 18013)
-- Dependencies: 1621
-- Name: fki_feedback_question; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fki_feedback_question ON feedbacks USING btree (id_question);


--
-- TOC entry 2093 (class 1259 OID 18012)
-- Dependencies: 1621
-- Name: fki_feedback_resource; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fki_feedback_resource ON feedbacks USING btree (id_resource);


--
-- TOC entry 2049 (class 1259 OID 17461)
-- Dependencies: 1601
-- Name: fki_id_question_pki; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fki_id_question_pki ON answers USING btree (id_question);


--
-- TOC entry 2062 (class 1259 OID 17462)
-- Dependencies: 1607
-- Name: fki_multichoice_question; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fki_multichoice_question ON questions_multichoice USING btree (id_question);


--
-- TOC entry 2056 (class 1259 OID 17463)
-- Dependencies: 1605
-- Name: fki_question_resource; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fki_question_resource ON questions USING btree (id_resource);


--
-- TOC entry 2067 (class 1259 OID 17464)
-- Dependencies: 1609
-- Name: fki_resources_user; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fki_resources_user ON resources USING btree (id_user);


--
-- TOC entry 2073 (class 1259 OID 17465)
-- Dependencies: 1613
-- Name: fki_sections_question; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fki_sections_question ON sections_questions USING btree (id_section);


--
-- TOC entry 2070 (class 1259 OID 17466)
-- Dependencies: 1612
-- Name: fki_sections_test; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fki_sections_test ON sections USING btree (id_test);


--
-- TOC entry 2074 (class 1259 OID 17467)
-- Dependencies: 1613
-- Name: fki_tests_test_sections; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fki_tests_test_sections ON sections_questions USING btree (id_question);


--
-- TOC entry 2079 (class 1259 OID 17469)
-- Dependencies: 1617
-- Name: fki_usuarios_tipo; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fki_usuarios_tipo ON users USING btree (id_type);


--
-- TOC entry 2057 (class 1259 OID 18695)
-- Dependencies: 1605
-- Name: id_category; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_category ON questions USING btree (id_category);


--
-- TOC entry 2058 (class 1259 OID 17471)
-- Dependencies: 1605
-- Name: id_createdby; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_createdby ON questions USING btree (id_createdby);


--
-- TOC entry 2059 (class 1259 OID 17472)
-- Dependencies: 1605
-- Name: id_modifiedby; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_modifiedby ON questions USING btree (id_modifiedby);


--
-- TOC entry 2139 (class 2606 OID 17473)
-- Dependencies: 1601 2068 1609
-- Name: answer_resource; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY answers
    ADD CONSTRAINT answer_resource FOREIGN KEY (id_resource) REFERENCES resources(id);


--
-- TOC entry 2141 (class 2606 OID 17478)
-- Dependencies: 2084 1603 1617
-- Name: caracteristicas_usuario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY categories
    ADD CONSTRAINT caracteristicas_usuario FOREIGN KEY (id_user) REFERENCES users(id);


--
-- TOC entry 2143 (class 2606 OID 19315)
-- Dependencies: 1603 1636 2106
-- Name: categories_visibility; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY categories
    ADD CONSTRAINT categories_visibility FOREIGN KEY (id_visibility) REFERENCES visibilities(id);


--
-- TOC entry 2176 (class 2606 OID 19457)
-- Dependencies: 1638 1638 2108
-- Name: categorytypes_parent; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY categorytypes
    ADD CONSTRAINT categorytypes_parent FOREIGN KEY (id_parent) REFERENCES categorytypes(id);


--
-- TOC entry 2178 (class 2606 OID 19531)
-- Dependencies: 1642 1601 2050
-- Name: dragdrop_answer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY answers_dragdrop
    ADD CONSTRAINT dragdrop_answer FOREIGN KEY (id_answer) REFERENCES answers(id) ON DELETE CASCADE;


--
-- TOC entry 2177 (class 2606 OID 19520)
-- Dependencies: 1641 2060 1605
-- Name: dragdrop_question; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY questions_dragdrop
    ADD CONSTRAINT dragdrop_question FOREIGN KEY (id_question) REFERENCES questions(id) ON DELETE CASCADE;


--
-- TOC entry 2179 (class 2606 OID 19536)
-- Dependencies: 1601 1642 2050
-- Name: dragdrop_right_answer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY answers_dragdrop
    ADD CONSTRAINT dragdrop_right_answer FOREIGN KEY (id_right) REFERENCES answers(id);


--
-- TOC entry 2188 (class 2606 OID 39069)
-- Dependencies: 1656 1660 2133
-- Name: evaluators_addresstypes; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY evaluators
    ADD CONSTRAINT evaluators_addresstypes FOREIGN KEY (id_addresstype) REFERENCES addresstypes(id);


--
-- TOC entry 2187 (class 2606 OID 38034)
-- Dependencies: 1656 1615 2077
-- Name: evaluators_tests; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY evaluators
    ADD CONSTRAINT evaluators_tests FOREIGN KEY (id_test) REFERENCES tests(id);


--
-- TOC entry 2170 (class 2606 OID 18284)
-- Dependencies: 1624 1621 2094
-- Name: feedback_feedbacktypes; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY feedbacks
    ADD CONSTRAINT feedback_feedbacktypes FOREIGN KEY (id_type) REFERENCES feedbacktypes(id);


--
-- TOC entry 2169 (class 2606 OID 17969)
-- Dependencies: 1605 1621 2060
-- Name: feedback_question; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY feedbacks
    ADD CONSTRAINT feedback_question FOREIGN KEY (id_question) REFERENCES questions(id) ON DELETE CASCADE;


--
-- TOC entry 2168 (class 2606 OID 17964)
-- Dependencies: 1609 2068 1621
-- Name: feedback_resource; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY feedbacks
    ADD CONSTRAINT feedback_resource FOREIGN KEY (id_resource) REFERENCES resources(id);


--
-- TOC entry 2147 (class 2606 OID 18696)
-- Dependencies: 1605 2052 1603
-- Name: id_category_pki; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY questions
    ADD CONSTRAINT id_category_pki FOREIGN KEY (id_category) REFERENCES categories(id);


--
-- TOC entry 2140 (class 2606 OID 17488)
-- Dependencies: 2060 1605 1601
-- Name: id_question_pki; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY answers
    ADD CONSTRAINT id_question_pki FOREIGN KEY (id_question) REFERENCES questions(id) ON DELETE CASCADE;


--
-- TOC entry 2152 (class 2606 OID 17493)
-- Dependencies: 1605 2060 1607
-- Name: multichoice_question; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY questions_multichoice
    ADD CONSTRAINT multichoice_question FOREIGN KEY (id_question) REFERENCES questions(id) ON DELETE CASCADE;


--
-- TOC entry 2189 (class 2606 OID 40736)
-- Dependencies: 1605 1661 2060
-- Name: omxml_question; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY questions_omxml
    ADD CONSTRAINT omxml_question FOREIGN KEY (id_question) REFERENCES questions(id) ON DELETE CASCADE;


--
-- TOC entry 2142 (class 2606 OID 17498)
-- Dependencies: 1603 2052 1603
-- Name: parent_category; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY categories
    ADD CONSTRAINT parent_category FOREIGN KEY (id_parent) REFERENCES categories(id);


--
-- TOC entry 2180 (class 2606 OID 28030)
-- Dependencies: 1646 1644 2117
-- Name: permissions_permissiontype; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY permissions
    ADD CONSTRAINT permissions_permissiontype FOREIGN KEY (id_type) REFERENCES permissiontypes(id);


--
-- TOC entry 2144 (class 2606 OID 17503)
-- Dependencies: 1617 2084 1605
-- Name: pki_createdby; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY questions
    ADD CONSTRAINT pki_createdby FOREIGN KEY (id_createdby) REFERENCES users(id);


--
-- TOC entry 2145 (class 2606 OID 17508)
-- Dependencies: 2084 1605 1617
-- Name: pki_modifiedby; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY questions
    ADD CONSTRAINT pki_modifiedby FOREIGN KEY (id_modifiedby) REFERENCES users(id);


--
-- TOC entry 2148 (class 2606 OID 39451)
-- Dependencies: 1609 1605 2068
-- Name: question_correct_feedback_resource; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY questions
    ADD CONSTRAINT question_correct_feedback_resource FOREIGN KEY (id_correct_feedback_resource) REFERENCES resources(id);


--
-- TOC entry 2151 (class 2606 OID 39466)
-- Dependencies: 2068 1605 1609
-- Name: question_final_feedback_resource; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY questions
    ADD CONSTRAINT question_final_feedback_resource FOREIGN KEY (id_final_feedback_resource) REFERENCES resources(id);


--
-- TOC entry 2149 (class 2606 OID 39456)
-- Dependencies: 1609 2068 1605
-- Name: question_incorrect_feedback_resource; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY questions
    ADD CONSTRAINT question_incorrect_feedback_resource FOREIGN KEY (id_incorrect_feedback_resource) REFERENCES resources(id);


--
-- TOC entry 2150 (class 2606 OID 39461)
-- Dependencies: 1609 1605 2068
-- Name: question_pass_feedback_resource; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY questions
    ADD CONSTRAINT question_pass_feedback_resource FOREIGN KEY (id_pass_feedback_resource) REFERENCES resources(id);


--
-- TOC entry 2146 (class 2606 OID 17513)
-- Dependencies: 1605 2068 1609
-- Name: question_resource; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY questions
    ADD CONSTRAINT question_resource FOREIGN KEY (id_resource) REFERENCES resources(id);


--
-- TOC entry 2191 (class 2606 OID 40896)
-- Dependencies: 2060 1605 1663
-- Name: questionresources_question; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY questionresources
    ADD CONSTRAINT questionresources_question FOREIGN KEY (id_question) REFERENCES questions(id) ON DELETE CASCADE;


--
-- TOC entry 2190 (class 2606 OID 40890)
-- Dependencies: 1609 2068 1663
-- Name: questionresources_resource; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY questionresources
    ADD CONSTRAINT questionresources_resource FOREIGN KEY (id_resource) REFERENCES resources(id);


--
-- TOC entry 2158 (class 2606 OID 17518)
-- Dependencies: 2060 1605 1613
-- Name: questions_section; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY sections_questions
    ADD CONSTRAINT questions_section FOREIGN KEY (id_question) REFERENCES questions(id);


--
-- TOC entry 2155 (class 2606 OID 19470)
-- Dependencies: 2052 1609 1603
-- Name: resources_category; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY resources
    ADD CONSTRAINT resources_category FOREIGN KEY (id_category) REFERENCES categories(id);


--
-- TOC entry 2156 (class 2606 OID 19491)
-- Dependencies: 2111 1609 1640
-- Name: resources_copyright; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY resources
    ADD CONSTRAINT resources_copyright FOREIGN KEY (id_copyright) REFERENCES copyrights(id);


--
-- TOC entry 2154 (class 2606 OID 17523)
-- Dependencies: 1609 1617 2084
-- Name: resources_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY resources
    ADD CONSTRAINT resources_user FOREIGN KEY (id_user) REFERENCES users(id);


--
-- TOC entry 2159 (class 2606 OID 17528)
-- Dependencies: 1613 1612 2071
-- Name: sections_question; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY sections_questions
    ADD CONSTRAINT sections_question FOREIGN KEY (id_section) REFERENCES sections(id);


--
-- TOC entry 2157 (class 2606 OID 17533)
-- Dependencies: 1612 2077 1615
-- Name: sections_test; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY sections
    ADD CONSTRAINT sections_test FOREIGN KEY (id_test) REFERENCES tests(id);


--
-- TOC entry 2172 (class 2606 OID 39074)
-- Dependencies: 1660 2133 1630
-- Name: supportcontacts_addresstypes; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY supportcontacts
    ADD CONSTRAINT supportcontacts_addresstypes FOREIGN KEY (id_addresstype) REFERENCES addresstypes(id);


--
-- TOC entry 2171 (class 2606 OID 18648)
-- Dependencies: 1615 1630 2077
-- Name: supportcontacts_test; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY supportcontacts
    ADD CONSTRAINT supportcontacts_test FOREIGN KEY (id_test) REFERENCES tests(id);


--
-- TOC entry 2174 (class 2606 OID 18685)
-- Dependencies: 1632 2071 1612
-- Name: testfeedbacks_section; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY testfeedbacks
    ADD CONSTRAINT testfeedbacks_section FOREIGN KEY (id_section) REFERENCES sections(id);


--
-- TOC entry 2173 (class 2606 OID 18680)
-- Dependencies: 2077 1615 1632
-- Name: testfeedbacks_test; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY testfeedbacks
    ADD CONSTRAINT testfeedbacks_test FOREIGN KEY (id_test) REFERENCES tests(id);


--
-- TOC entry 2175 (class 2606 OID 18690)
-- Dependencies: 2104 1634 1632
-- Name: testfeedbacks_unit; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY testfeedbacks
    ADD CONSTRAINT testfeedbacks_unit FOREIGN KEY (id_unit) REFERENCES scoreunits(id);


--
-- TOC entry 2165 (class 2606 OID 37749)
-- Dependencies: 1615 1654 2127
-- Name: tests_assessement; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY tests
    ADD CONSTRAINT tests_assessement FOREIGN KEY (id_assessement) REFERENCES assessements(id);


--
-- TOC entry 2164 (class 2606 OID 18712)
-- Dependencies: 1615 1603 2052
-- Name: tests_category; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY tests
    ADD CONSTRAINT tests_category FOREIGN KEY (id_category) REFERENCES categories(id);


--
-- TOC entry 2160 (class 2606 OID 18620)
-- Dependencies: 1617 2084 1615
-- Name: tests_createdby; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY tests
    ADD CONSTRAINT tests_createdby FOREIGN KEY (id_createdby) REFERENCES users(id);


--
-- TOC entry 2161 (class 2606 OID 18625)
-- Dependencies: 1617 1615 2084
-- Name: tests_modifiedby; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY tests
    ADD CONSTRAINT tests_modifiedby FOREIGN KEY (id_modifiedby) REFERENCES users(id);


--
-- TOC entry 2162 (class 2606 OID 18630)
-- Dependencies: 1615 1626 2096
-- Name: tests_navlocation; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY tests
    ADD CONSTRAINT tests_navlocation FOREIGN KEY (id_navlocation) REFERENCES navlocations(id);


--
-- TOC entry 2163 (class 2606 OID 18635)
-- Dependencies: 1615 2098 1628
-- Name: tests_redoquestionvalue; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY tests
    ADD CONSTRAINT tests_redoquestionvalue FOREIGN KEY (id_redoquestion) REFERENCES redoquestionvalues(id);


--
-- TOC entry 2166 (class 2606 OID 38130)
-- Dependencies: 2131 1658 1615
-- Name: tests_scoretype; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY tests
    ADD CONSTRAINT tests_scoretype FOREIGN KEY (id_scoretype) REFERENCES scoretypes(id);


--
-- TOC entry 2185 (class 2606 OID 37176)
-- Dependencies: 1615 2077 1651
-- Name: testusers_test; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY testusers
    ADD CONSTRAINT testusers_test FOREIGN KEY (id_test) REFERENCES tests(id);


--
-- TOC entry 2186 (class 2606 OID 37181)
-- Dependencies: 2084 1617 1651
-- Name: testusers_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY testusers
    ADD CONSTRAINT testusers_user FOREIGN KEY (id_user) REFERENCES users(id);


--
-- TOC entry 2153 (class 2606 OID 17543)
-- Dependencies: 2060 1605 1608
-- Name: truefalse_question; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY questions_truefalse
    ADD CONSTRAINT truefalse_question FOREIGN KEY (id_question) REFERENCES questions(id) ON DELETE CASCADE;


--
-- TOC entry 2184 (class 2606 OID 28072)
-- Dependencies: 2119 1646 1650
-- Name: userpermissions_permission; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY userpermissions
    ADD CONSTRAINT userpermissions_permission FOREIGN KEY (id_permission) REFERENCES permissions(id);


--
-- TOC entry 2183 (class 2606 OID 28067)
-- Dependencies: 2084 1617 1650
-- Name: userpermissions_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY userpermissions
    ADD CONSTRAINT userpermissions_user FOREIGN KEY (id_user) REFERENCES users(id);


--
-- TOC entry 2182 (class 2606 OID 28051)
-- Dependencies: 2119 1646 1648
-- Name: usertypepermissions_permission; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY usertypepermissions
    ADD CONSTRAINT usertypepermissions_permission FOREIGN KEY (id_permission) REFERENCES permissions(id);


--
-- TOC entry 2181 (class 2606 OID 28046)
-- Dependencies: 1648 2086 1619
-- Name: usertypepermissions_usertype; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY usertypepermissions
    ADD CONSTRAINT usertypepermissions_usertype FOREIGN KEY (id_usertype) REFERENCES usertypes(id);


--
-- TOC entry 2167 (class 2606 OID 17548)
-- Dependencies: 1617 2086 1619
-- Name: usuarios_tipo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY users
    ADD CONSTRAINT usuarios_tipo FOREIGN KEY (id_type) REFERENCES usertypes(id);


--
-- TOC entry 2196 (class 0 OID 0)
-- Dependencies: 3
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

--
-- TOC entry 2193 (class 0 OID 0)
-- Dependencies: 1652
-- Name: addresstypes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('addresstypes_id_seq', 5, true);


--
-- TOC entry 2195 (class 0 OID 0)
-- Dependencies: 1646
-- Name: assessements_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('assessements_id_seq', 3, true);


--
-- TOC entry 2196 (class 0 OID 0)
-- Dependencies: 1597
-- Name: categories_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('categories_id_seq', 1, true);


--
-- TOC entry 2197 (class 0 OID 0)
-- Dependencies: 1630
-- Name: categorytypes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('categorytypes_id_seq', 4, true);


--
-- TOC entry 2198 (class 0 OID 0)
-- Dependencies: 1632
-- Name: copyrights_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('copyrights_id_seq', 13, true);



--
-- TOC entry 2201 (class 0 OID 0)
-- Dependencies: 1616
-- Name: feedbacktypes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('feedbacktypes_id_seq', 2, true);


--
-- TOC entry 2202 (class 0 OID 0)
-- Dependencies: 1618
-- Name: navlocations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('navlocations_id_seq', 3, true);


--
-- TOC entry 2203 (class 0 OID 0)
-- Dependencies: 1638
-- Name: permissions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('permissions_id_seq', 128, true);


--
-- TOC entry 2204 (class 0 OID 0)
-- Dependencies: 1636
-- Name: permissiontypes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('permissiontypes_id_seq', 3, true);


--
-- TOC entry 2206 (class 0 OID 0)
-- Dependencies: 1620
-- Name: redoquestionvalues_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('redoquestionvalues_id_seq', 3, true);


--
-- TOC entry 2208 (class 0 OID 0)
-- Dependencies: 1650
-- Name: scoretypes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('scoretypes_id_seq', 2, true);


--
-- TOC entry 2209 (class 0 OID 0)
-- Dependencies: 1626
-- Name: scoreunits_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('scoreunits_id_seq', 2, true);


--
-- TOC entry 2216 (class 0 OID 0)
-- Dependencies: 1642
-- Name: userpermissions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('userpermissions_id_seq', 50, true);


--
-- TOC entry 2217 (class 0 OID 0)
-- Dependencies: 1611
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('users_id_seq', 1, true);


--
-- TOC entry 2218 (class 0 OID 0)
-- Dependencies: 1640
-- Name: usertypepermissions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('usertypepermissions_id_seq', 25, true);


--
-- TOC entry 2219 (class 0 OID 0)
-- Dependencies: 1613
-- Name: usertypes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('usertypes_id_seq', 1, true);


--
-- TOC entry 2220 (class 0 OID 0)
-- Dependencies: 1628
-- Name: visibilities_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('visibilities_id_seq', 3, true);


--
-- TOC entry 2190 (class 0 OID 39063)
-- Dependencies: 1653
-- Data for Name: addresstypes; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO addresstypes (id, type, subtype) VALUES (1, 'NO_FILTER', '');
INSERT INTO addresstypes (id, type, subtype) VALUES (2, 'USER_FILTER', 'USERS_SELECTION');
INSERT INTO addresstypes (id, type, subtype) VALUES (3, 'USER_FILTER', 'RANGE_NAME');
INSERT INTO addresstypes (id, type, subtype) VALUES (4, 'USER_FILTER', 'RANGE_SURNAME');
INSERT INTO addresstypes (id, type, subtype) VALUES (5, 'GROUP_FILTER', '');


--
-- TOC entry 2169 (class 0 OID 17424)
-- Dependencies: 1612
-- Data for Name: usertypes; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO usertypes (type, id, description) VALUES ('Administrador', 1, 'Con permisos para labores de administración');


--
-- TOC entry 2168 (class 0 OID 17416)
-- Dependencies: 1610 2169
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO users (login, password, name, surname, id, id_type, nick, gepeq_user, oucu) VALUES ('profesor', 'U235Wo3HNdUDbt9ZrBnRqVBPNZdQ99FaFMk/F+wotLA3XFU/8Vxan4+17kEh13FDYXFN6iP4w8/dN7ssNeEgbg==', 'Profesor', 'Profesor', 1, 1, 'Profesor', true, 'profesor');


--
-- TOC entry 2177 (class 0 OID 19129)
-- Dependencies: 1629
-- Data for Name: visibilities; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO visibilities (id, visibility, global, level) VALUES (1, 'CATEGORY_VISIBILITY_GLOBAL', true, 0);
INSERT INTO visibilities (id, visibility, global, level) VALUES (2, 'CATEGORY_VISIBILITY_PUBLIC', false, 0);
INSERT INTO visibilities (id, visibility, global, level) VALUES (3, 'CATEGORY_VISIBILITY_PRIVATE', false, 1);


--
-- TOC entry 2160 (class 0 OID 17362)
-- Dependencies: 1596 2168 2177
-- Data for Name: categories; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO categories (name, id_user, id, id_parent, description, default_category, id_type, id_visibility) VALUES ('DEFAULT_CATEGORY', 1, 1, NULL, 'SYSTEM_CATEGORY', true, 1, 3);


--
-- TOC entry 2179 (class 0 OID 19485)
-- Dependencies: 1633
-- Data for Name: copyrights; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO copyrights (id, copyright) VALUES (1, 'COPYRIGHT_EUPL');
INSERT INTO copyrights (id, copyright) VALUES (2, 'COPYRIGHT_GPL');
INSERT INTO copyrights (id, copyright) VALUES (3, 'COPYRIGHT_DUAL_GPL_EUPL');
INSERT INTO copyrights (id, copyright) VALUES (4, 'COPYRIGHT_PUBLIC_DOMAIN');
INSERT INTO copyrights (id, copyright) VALUES (5, 'COPYRIGHT_PRIVATE_LICENSE');
INSERT INTO copyrights (id, copyright) VALUES (6, 'COPYRIGHT_CREATIVE_COMMONS_CC_BY');
INSERT INTO copyrights (id, copyright) VALUES (7, 'COPYRIGHT_CREATIVE_COMMONS_CC_BY_SA');
INSERT INTO copyrights (id, copyright) VALUES (8, 'COPYRIGHT_CREATIVE_COMMONS_CC_BY_ND');
INSERT INTO copyrights (id, copyright) VALUES (9, 'COPYRIGHT_CREATIVE_COMMONS_CC_BY_NC');
INSERT INTO copyrights (id, copyright) VALUES (10, 'COPYRIGHT_CREATIVE_COMMONS_CC_BY_NC_SA');
INSERT INTO copyrights (id, copyright) VALUES (11, 'COPYRIGHT_CREATIVE_COMMONS_CC_BY_NC_ND');
INSERT INTO copyrights (id, copyright) VALUES (12, 'COPYRIGHT_GFDL');
INSERT INTO copyrights (id, copyright) VALUES (13, 'COPYRIGHT_OTHER_FREE');


--
-- TOC entry 2187 (class 0 OID 37738)
-- Dependencies: 1647
-- Data for Name: assessements; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO assessements (id, type) VALUES (1, 'ASSESSEMENT_NOT_ASSESSED');
INSERT INTO assessements (id, type) VALUES (2, 'ASSESSEMENT_REQUIRED');


--
-- TOC entry 2178 (class 0 OID 19451)
-- Dependencies: 1631
-- Data for Name: categorytypes; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO categorytypes (id, type, icon, id_parent) VALUES (1, 'CATEGORY_TYPE_GENERAL', 'ui-icon ui-icon-star', NULL);
INSERT INTO categorytypes (id, type, icon, id_parent) VALUES (2, 'CATEGORY_TYPE_QUESTIONS', 'ui-icon ui-icon-help', 1);
INSERT INTO categorytypes (id, type, icon, id_parent) VALUES (3, 'CATEGORY_TYPE_TESTS', 'ui-icon ui-icon-document', 1);
INSERT INTO categorytypes (id, type, icon, id_parent) VALUES (4, 'CATEGORY_TYPE_IMAGES', 'ui-icon ui-icon-image', 1);


--
-- TOC entry 2172 (class 0 OID 18606)
-- Dependencies: 1619
-- Data for Name: navlocations; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO navlocations (id, location) VALUES (1, 'NAVLOCATION_LEFT');
INSERT INTO navlocations (id, location) VALUES (2, 'NAVLOCATION_BOTTOM');
INSERT INTO navlocations (id, location) VALUES (3, 'NAVLOCATION_WIDE');


--
-- TOC entry 2173 (class 0 OID 18614)
-- Dependencies: 1621
-- Data for Name: redoquestionvalues; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO redoquestionvalues (id, value) VALUES (1, 'YES');
INSERT INTO redoquestionvalues (id, value) VALUES (2, 'NO');
INSERT INTO redoquestionvalues (id, value) VALUES (3, 'ASK');


--
-- TOC entry 2189 (class 0 OID 38124)
-- Dependencies: 1651
-- Data for Name: scoretypes; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO scoretypes (id, type) VALUES (1, 'SCORE_TYPE_QUESTIONS');
INSERT INTO scoretypes (id, type) VALUES (2, 'SCORE_TYPE_SECTIONS');


--
-- TOC entry 2171 (class 0 OID 18278)
-- Dependencies: 1617
-- Data for Name: feedbacktypes; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO feedbacktypes (id, type) VALUES (1, 'FEEDBACK_TYPE_NORMAL');
INSERT INTO feedbacktypes (id, type) VALUES (2, 'FEEDBACK_TYPE_FIXED');


--
-- TOC entry 2182 (class 0 OID 28013)
-- Dependencies: 1637
-- Data for Name: permissiontypes; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO permissiontypes (id, type) VALUES (1, 'PERMISSION_TYPE_BOOLEAN');
INSERT INTO permissiontypes (id, type) VALUES (2, 'PERMISSION_TYPE_INT');
INSERT INTO permissiontypes (id, type) VALUES (3, 'PERMISSION_TYPE_STRING');


--
-- TOC entry 2183 (class 0 OID 28021)
-- Dependencies: 1639 2182
-- Data for Name: permissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO permissions (id, name, id_type, default_value) VALUES (1, 'PERMISSION_NAVIGATION_CATEGORIES', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (2, 'PERMISSION_NAVIGATION_RESOURCES', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (3, 'PERMISSION_NAVIGATION_QUESTIONS', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (4, 'PERMISSION_NAVIGATION_TESTS', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (5, 'PERMISSION_NAVIGATION_IMPORT', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (6, 'PERMISSION_NAVIGATION_EXPORT', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (7, 'PERMISSION_NAVIGATION_ADMINISTRATION', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (8, 'PERMISSION_CATEGORIES_GLOBAL_FILTER_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (9, 'PERMISSION_CATEGORIES_OTHER_USERS_FILTER_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (10, 'PERMISSION_CATEGORIES_EDIT_MODE_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (11, 'PERMISSION_CATEGORIES_ADD_MODE_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (12, 'PERMISSION_CATEGORIES_ADD_GLOBAL_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (13, 'PERMISSION_CATEGORIES_DELETE_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (14, 'PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (15, 'PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (16, 'PERMISSION_RESOURCES_ADD_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (17, 'PERMISSION_RESOURCES_EDIT_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (18, 'PERMISSION_RESOURCES_DELETE_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (19, 'PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (20, 'PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (21, 'PERMISSION_QUESTIONS_ADD_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (22, 'PERMISSION_QUESTIONS_EDIT_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (23, 'PERMISSION_QUESTIONS_DELETE_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (24, 'PERMISSION_QUESTIONS_VIEW_OM_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (25, 'PERMISSION_TESTS_GLOBAL_FILTER_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (26, 'PERMISSION_TESTS_OTHER_USERS_FILTER_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (27, 'PERMISSION_TESTS_ADD_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (28, 'PERMISSION_TESTS_EDIT_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (29, 'PERMISSION_TESTS_DELETE_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (30, 'PERMISSION_TESTS_VIEW_OM_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (31, 'PERMISSION_ADMINISTRATION_ADMIN_USERS', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (32, 'PERMISSION_ADMINISTRATION_ADMIN_ROLES', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (33, 'PERMISSION_ADMINISTRATION_ADD_USER_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (34, 'PERMISSION_ADMINISTRATION_EDIT_USER_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (35, 'PERMISSION_ADMINISTRATION_DELETE_USER_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (36, 'PERMISSION_ADMINISTRATION_ADD_ROLE_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (37, 'PERMISSION_ADMINISTRATION_EDIT_ROLE_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (38, 'PERMISSION_ADMINISTRATION_DELETE_ROLE_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (39, 'PERMISSION_RESOURCE_GLOBAL_OTHER_USER_CATEGORY_ALLOWED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (40, 'PERMISSION_RESOURCE_LOCAL_SOURCE_ALLOWED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (41, 'PERMISSION_RESOURCE_NETWORK_SOURCE_ALLOWED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (42, 'PERMISSION_RESOURCE_UPLOAD_SIZE_LIMIT', 2, '2097152');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (43, 'PERMISSION_RESOURCE_MAXIMUM_SPACE_LIMIT', 2, '52428800');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (44, 'PERMISSION_QUESTION_GLOBAL_OTHER_USER_CATEGORY_ALLOWED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (45, 'PERMISSION_TEST_GLOBAL_OTHER_USER_CATEGORY_ALLOWED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (46, 'PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (47, 'PERMISSION_ADMINISTRATION_EDIT_ADMINS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (48, 'PERMISSION_ADMINISTRATION_EDIT_SUPERADMINS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (49, 'PERMISSION_ADMINISTRATION_DELETE_ADMINS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (50, 'PERMISSION_ADMINISTRATION_DELETE_SUPERADMINS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (51, 'PERMISSION_ADMINISTRATION_EDIT_ADMIN_ROLES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (52, 'PERMISSION_ADMINISTRATION_EDIT_SUPERADMIN_ROLES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (53, 'PERMISSION_ADMINISTRATION_DELETE_ADMIN_ROLES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (54, 'PERMISSION_ADMINISTRATION_DELETE_SUPERADMIN_ROLES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (55, 'PERMISSION_CATEGORIES_EDIT_OTHER_USERS_CATEGORIES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (56, 'PERMISSION_CATEGORIES_EDIT_ADMINS_CATEGORIES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (57, 'PERMISSION_CATEGORIES_EDIT_SUPERADMINS_CATEGORIES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (58, 'PERMISSION_CATEGORIES_DELETE_OTHER_USERS_CATEGORIES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (59, 'PERMISSION_CATEGORIES_DELETE_ADMINS_CATEGORIES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (60, 'PERMISSION_CATEGORIES_DELETE_SUPERADMINS_CATEGORIES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (61, 'PERMISSION_CATEGORIES_VIEW_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (62, 'PERMISSION_CATEGORIES_VIEW_ADMINS_PRIVATE_CATEGORIES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (63, 'PERMISSION_CATEGORIES_VIEW_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (64, 'PERMISSION_RESOURCES_EDIT_OTHER_USERS_RESOURCES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (65, 'PERMISSION_RESOURCES_EDIT_ADMINS_RESOURCES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (66, 'PERMISSION_RESOURCES_EDIT_SUPERADMINS_RESOURCES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (67, 'PERMISSION_RESOURCES_DELETE_OTHER_USERS_RESOURCES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (68, 'PERMISSION_RESOURCES_DELETE_ADMINS_RESOURCES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (69, 'PERMISSION_RESOURCES_DELETE_SUPERADMINS_RESOURCES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (70, 'PERMISSION_RESOURCES_VIEW_RESOURCES_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (71, 'PERMISSION_RESOURCES_VIEW_RESOURCES_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (72, 'PERMISSION_RESOURCES_VIEW_RESOURCES_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (73, 'PERMISSION_RESOURCE_OTHER_USER_USE_BETTER_UPLOAD_SIZE_LIMIT', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (74, 'PERMISSION_RESOURCE_OTHER_USER_USE_BETTER_MAXIMUM_SPACE_LIMIT', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (75, 'PERMISSION_QUESTIONS_EDIT_OTHER_USERS_QUESTIONS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (76, 'PERMISSION_QUESTIONS_EDIT_ADMINS_QUESTIONS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (77, 'PERMISSION_QUESTIONS_EDIT_SUPERADMINS_QUESTIONS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (78, 'PERMISSION_QUESTIONS_DELETE_OTHER_USERS_QUESTIONS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (79, 'PERMISSION_QUESTIONS_DELETE_ADMINS_QUESTIONS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (80, 'PERMISSION_QUESTIONS_DELETE_SUPERADMINS_QUESTIONS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (81, 'PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (82, 'PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (83, 'PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (84, 'PERMISSION_QUESTION_USE_GLOBAL_RESOURCES', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (85, 'PERMISSION_QUESTION_USE_OTHER_USERS_RESOURCES', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (86, 'PERMISSION_TESTS_EDIT_OTHER_USERS_TESTS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (87, 'PERMISSION_TESTS_EDIT_ADMINS_TESTS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (88, 'PERMISSION_TESTS_EDIT_SUPERADMINS_TESTS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (89, 'PERMISSION_TESTS_DELETE_OTHER_USERS_TESTS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (90, 'PERMISSION_TESTS_DELETE_ADMINS_TESTS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (91, 'PERMISSION_TESTS_DELETE_SUPERADMINS_TESTS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (92, 'PERMISSION_TESTS_VIEW_TESTS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (93, 'PERMISSION_TESTS_VIEW_TESTS_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (94, 'PERMISSION_TESTS_VIEW_TESTS_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (95, 'PERMISSION_TEST_USE_GLOBAL_QUESTIONS', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (96, 'PERMISSION_TEST_USE_OTHER_USERS_QUESTIONS', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (97, 'PERMISSION_OM_LOGIN_AS_OTHER_USER_FOR_PREVIEW_TESTS_ALLOWED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (98, 'PERMISSION_NAVIGATION_PUBLICATION', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (99, 'PERMISSION_PUBLICATION_QUESTIONS', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (100, 'PERMISSION_PUBLICATION_TESTS', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (101, 'PERMISSION_PUBLICATION_PUBLISH_QUESTIONS_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (102, 'PERMISSION_PUBLICATION_PUBLISH_OTHER_USERS_QUESTIONS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (103, 'PERMISSION_PUBLICATION_PUBLISH_ADMINS_QUESTIONS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (104, 'PERMISSION_PUBLICATION_PUBLISH_SUPERADMINS_QUESTIONS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (125, 'PERMISSION_TESTS_CREATE_COPY_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (126, 'PERMISSION_TESTS_CREATE_COPY_FROM_OTHER_USERS_NON_EDITABLE_TESTS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (127, 'PERMISSION_TESTS_CREATE_COPY_FROM_ADMINS_NON_EDITABLE_TESTS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (128, 'PERMISSION_TESTS_CREATE_COPY_FROM_SUPERADMINS_NON_EDITABLE_TESTS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (105, 'PERMISSION_PUBLICATION_PUBLISH_TESTS_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (106, 'PERMISSION_PUBLICATION_PUBLISH_OTHER_USERS_TESTS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (107, 'PERMISSION_PUBLICATION_PUBLISH_ADMINS_TESTS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (108, 'PERMISSION_PUBLICATION_PUBLISH_SUPERADMINS_TESTS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (109, 'PERMISSION_PUBLICATION_UNPUBLISH_QUESTION_RELEASES_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (110, 'PERMISSION_PUBLICATION_UNPUBLISH_OTHER_USERS_QUESTION_RELEASES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (111, 'PERMISSION_PUBLICATION_UNPUBLISH_ADMINS_QUESTION_RELEASES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (112, 'PERMISSION_PUBLICATION_UNPUBLISH_SUPERADMINS_QUESTION_RELEASES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (113, 'PERMISSION_PUBLICATION_UNPUBLISH_TEST_RELEASES_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (114, 'PERMISSION_PUBLICATION_UNPUBLISH_OTHER_USERS_TEST_RELEASES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (115, 'PERMISSION_PUBLICATION_UNPUBLISH_ADMINS_TEST_RELEASES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (116, 'PERMISSION_PUBLICATION_UNPUBLISH_SUPERADMINS_TEST_RELEASES_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (117, 'PERMISSION_PUBLICATION_UNPUBLISH_QUESTION_RELEASES_OPENED_WITH_CLOSE_DATE_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (118, 'PERMISSION_PUBLICATION_UNPUBLISH_TEST_RELEASES_OPENED_WITH_CLOSE_DATE_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (119, 'PERMISSION_PUBLICATION_UNPUBLISH_QUESTION_RELEASES_BEFORE_DELETE_DATE_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (120, 'PERMISSION_PUBLICATION_UNPUBLISH_TEST_RELEASES_BEFORE_DELETE_DATE_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (121, 'PERMISSION_QUESTIONS_CREATE_COPY_ENABLED', 1, 'true');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (122, 'PERMISSION_QUESTIONS_CREATE_COPY_FROM_OTHER_USERS_NON_EDITABLE_QUESTIONS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (123, 'PERMISSION_QUESTIONS_CREATE_COPY_FROM_ADMINS_NON_EDITABLE_QUESTIONS_ENABLED', 1, 'false');
INSERT INTO permissions (id, name, id_type, default_value) VALUES (124, 'PERMISSION_QUESTIONS_CREATE_COPY_FROM_SUPERADMINS_NON_EDITABLE_QUESTIONS_ENABLED', 1, 'false');


--
-- TOC entry 2176 (class 0 OID 18666)
-- Dependencies: 1627
-- Data for Name: scoreunits; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO scoreunits (id, unit) VALUES (1, 'MARK_UNITS');
INSERT INTO scoreunits (id, unit) VALUES (2, 'PERCENTAGE_UNITS');


--
-- TOC entry 2185 (class 0 OID 28058)
-- Dependencies: 1643 2168 2183
-- Data for Name: userpermissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (1, 1, 46, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (2, 1, 51, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (3, 1, 52, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (4, 1, 47, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (5, 1, 48, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (6, 1, 53, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (7, 1, 54, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (8, 1, 49, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (9, 1, 50, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (10, 1, 56, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (11, 1, 57, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (12, 1, 59, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (13, 1, 60, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (14, 1, 62, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (15, 1, 63, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (16, 1, 65, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (17, 1, 66, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (18, 1, 68, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (19, 1, 69, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (20, 1, 72, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (21, 1, 71, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (22, 1, 74, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (23, 1, 76, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (24, 1, 77, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (25, 1, 79, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (26, 1, 80, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (27, 1, 82, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (28, 1, 83, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (29, 1, 87, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (30, 1, 88, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (31, 1, 90, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (32, 1, 91, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (33, 1, 93, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (34, 1, 94, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (35, 1, 103, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (36, 1, 104, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (37, 1, 107, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (38, 1, 108, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (39, 1, 117, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (40, 1, 111, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (41, 1, 112, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (42, 1, 118, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (43, 1, 115, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (44, 1, 116, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (45, 1, 119, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (46, 1, 120, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (47, 1, 123, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (48, 1, 124, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (49, 1, 127, 'true');
INSERT INTO userpermissions (id, id_user, id_permission, value) VALUES (50, 1, 128, 'true');


--
-- TOC entry 2184 (class 0 OID 28037)
-- Dependencies: 1641 2169 2183
-- Data for Name: usertypepermissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (1, 1, 7, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (2, 1, 12, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (3, 1, 42, '0');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (4, 1, 43, '0');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (5, 1, 55, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (6, 1, 58, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (7, 1, 61, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (8, 1, 64, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (9, 1, 67, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (10, 1, 70, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (11, 1, 73, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (12, 1, 75, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (13, 1, 78, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (14, 1, 81, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (15, 1, 86, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (16, 1, 89, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (17, 1, 92, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (18, 1, 97, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (19, 1, 98, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (20, 1, 106, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (21, 1, 102, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (22, 1, 110, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (23, 1, 114, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (24, 1, 122, 'true');
INSERT INTO usertypepermissions (id, id_usertype, id_permission, value) VALUES (25, 1, 126, 'true');


-- Completed on 2013-07-02 09:32:29

--
-- PostgreSQL database dump complete
--

