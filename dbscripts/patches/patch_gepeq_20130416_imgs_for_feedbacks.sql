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

ALTER TABLE questions ADD COLUMN id_correct_feedback_resource bigint;
ALTER TABLE questions ADD COLUMN id_incorrect_feedback_resource bigint;
ALTER TABLE questions ADD COLUMN id_pass_feedback_resource bigint;
ALTER TABLE questions ADD COLUMN id_final_feedback_resource bigint;
ALTER TABLE questions ADD COLUMN correct_feedback_resource_width integer;
UPDATE questions SET correct_feedback_resource_width=-1;
ALTER TABLE questions ALTER COLUMN correct_feedback_resource_width SET NOT NULL;
ALTER TABLE questions ALTER COLUMN correct_feedback_resource_width SET DEFAULT (-1);
ALTER TABLE questions ADD COLUMN correct_feedback_resource_height integer;
UPDATE questions SET correct_feedback_resource_height=-1;
ALTER TABLE questions ALTER COLUMN correct_feedback_resource_height SET NOT NULL;
ALTER TABLE questions ALTER COLUMN correct_feedback_resource_height SET DEFAULT (-1);
ALTER TABLE questions ADD COLUMN incorrect_feedback_resource_width integer;
UPDATE questions SET incorrect_feedback_resource_width=-1;
ALTER TABLE questions ALTER COLUMN incorrect_feedback_resource_width SET NOT NULL;
ALTER TABLE questions ALTER COLUMN incorrect_feedback_resource_width SET DEFAULT (-1);
ALTER TABLE questions ADD COLUMN incorrect_feedback_resource_height integer;
UPDATE questions SET incorrect_feedback_resource_height=-1;
ALTER TABLE questions ALTER COLUMN incorrect_feedback_resource_height SET NOT NULL;
ALTER TABLE questions ALTER COLUMN incorrect_feedback_resource_height SET DEFAULT (-1);
ALTER TABLE questions ADD COLUMN pass_feedback_resource_width integer;
UPDATE questions SET pass_feedback_resource_width=-1;
ALTER TABLE questions ALTER COLUMN pass_feedback_resource_width SET NOT NULL;
ALTER TABLE questions ALTER COLUMN pass_feedback_resource_width SET DEFAULT (-1);
ALTER TABLE questions ADD COLUMN pass_feedback_resource_height integer;
UPDATE questions SET pass_feedback_resource_height=-1;
ALTER TABLE questions ALTER COLUMN pass_feedback_resource_height SET NOT NULL;
ALTER TABLE questions ALTER COLUMN pass_feedback_resource_height SET DEFAULT (-1);
ALTER TABLE questions ADD COLUMN final_feedback_resource_width integer;
UPDATE questions SET final_feedback_resource_width=-1;
ALTER TABLE questions ALTER COLUMN final_feedback_resource_width SET NOT NULL;
ALTER TABLE questions ALTER COLUMN final_feedback_resource_width SET DEFAULT (-1);
ALTER TABLE questions ADD COLUMN final_feedback_resource_height integer;
UPDATE questions SET final_feedback_resource_height=-1;
ALTER TABLE questions ALTER COLUMN final_feedback_resource_height SET NOT NULL;
ALTER TABLE questions ALTER COLUMN final_feedback_resource_height SET DEFAULT (-1);
ALTER TABLE questions
  ADD CONSTRAINT question_correct_feedback_resource FOREIGN KEY (id_correct_feedback_resource)
      REFERENCES resources (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE questions
  ADD CONSTRAINT question_incorrect_feedback_resource FOREIGN KEY (id_incorrect_feedback_resource)
      REFERENCES resources (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE questions
  ADD CONSTRAINT question_pass_feedback_resource FOREIGN KEY (id_pass_feedback_resource)
      REFERENCES resources (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE questions
  ADD CONSTRAINT question_final_feedback_resource FOREIGN KEY (id_final_feedback_resource)
      REFERENCES resources (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;
