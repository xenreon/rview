/*
 * Copyright (C) 2016 Jorge Ruesga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ruesga.rview.gerrit.model;

import com.google.gson.annotations.Since;

public enum ChangeOptions {
    LABELS, DETAILED_LABELS, CURRENT_REVISION, ALL_REVISIONS, DOWNLOAD_COMMANDS, CURRENT_COMMIT,
    ALL_COMMITS, CURRENT_FILES, ALL_FILES, DETAILED_ACCOUNTS, @Since(2.13)REVIEWER_UPDATES,
    MESSAGES, CURRENT_ACTIONS, CHANGE_ACTIONS, REVIEWED, WEB_LINKS, CHECK,
    @Since(2.12)COMMIT_FOOTERS, @Since(2.12) PUSH_CERTIFICATES, @Since(2.15) TRACKING_IDS
}
