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

ALTER TABLE questions DROP CONSTRAINT question_final_feedback_resource;
ALTER TABLE questions DROP CONSTRAINT question_pass_feedback_resource;
ALTER TABLE questions DROP CONSTRAINT question_incorrect_feedback_resource;
ALTER TABLE questions DROP CONSTRAINT question_correct_feedback_resource;
ALTER TABLE questions DROP COLUMN final_feedback_resource_height;
ALTER TABLE questions DROP COLUMN final_feedback_resource_width;
ALTER TABLE questions DROP COLUMN pass_feedback_resource_height;
ALTER TABLE questions DROP COLUMN pass_feedback_resource_width;
ALTER TABLE questions DROP COLUMN incorrect_feedback_resource_height;
ALTER TABLE questions DROP COLUMN incorrect_feedback_resource_width;
ALTER TABLE questions DROP COLUMN correct_feedback_resource_height;
ALTER TABLE questions DROP COLUMN correct_feedback_resource_width;
ALTER TABLE questions DROP COLUMN id_final_feedback_resource;
ALTER TABLE questions DROP COLUMN id_pass_feedback_resource;
ALTER TABLE questions DROP COLUMN id_incorrect_feedback_resource;
ALTER TABLE questions DROP COLUMN id_correct_feedback_resource;
