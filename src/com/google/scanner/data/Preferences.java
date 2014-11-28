/*
 * Copyright (C) 2008 ZXing authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.scanner.data;

public class Preferences {

    private Preferences() {
    };

    public static boolean KEY_DECODE_1D = true;
    public static boolean KEY_DECODE_QR = true;
    public static boolean KEY_DECODE_DATA_MATRIX = true;

    public static boolean KEY_REVERSE_IMAGE = false;
    public static boolean KEY_FRONT_LIGHT = false;
}
