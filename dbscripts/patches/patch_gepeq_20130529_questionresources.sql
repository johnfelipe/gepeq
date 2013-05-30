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

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

\connect gepeq

SET search_path = public, pg_catalog;

CREATE TABLE questionresources
(
  id bigserial NOT NULL,
  id_question bigint NOT NULL,
  id_resource bigint NOT NULL,
  "position" integer NOT NULL,
  "name" character varying(15),
  width integer NOT NULL DEFAULT (-1),
  height integer NOT NULL DEFAULT (-1),
  CONSTRAINT questionresources_pk PRIMARY KEY (id),
  CONSTRAINT questionresources_question FOREIGN KEY (id_question)
      REFERENCES questions (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT questionresources_resource FOREIGN KEY (id_resource)
      REFERENCES resources (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE SEQUENCE questionresources_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

ALTER TABLE questionresources ALTER COLUMN id SET DEFAULT nextval('questionresources_id_seq'::regclass);
