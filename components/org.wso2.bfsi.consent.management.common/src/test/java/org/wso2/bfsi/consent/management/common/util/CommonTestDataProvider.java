package org.wso2.bfsi.consent.management.common.util;

import org.testng.annotations.DataProvider;

public class CommonTestDataProvider {

    public static final String JWT_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJQUzI1NiIsImtpZCI6IkdxaEtWVEFObkxNWXBHR2ZBd" +
            "EoxTmhka2dqdyJ9.eyJpc3MiOiJ1M1pXbGY5WXQ0MmR5WmdJdnprdnFiIiwiaWF0IjoxNTcxODA4MTY3LCJleHAiOjIxNDc0ODM2" +
            "NDYsImp0aSI6IjM3NzQ3Y2QxYzEwNTQ1Njk5Zjc1NGFkZjI4YjczZTMxIiwiYXVkIjoiaHR0cHM6Ly9zZWN1cmUuYXBpLmRhdGFo" +
            "b2xkZXIuY29tL2lzc3VlciIsInJlZGlyZWN0X3VyaXMiOlsiaHR0cHM6Ly93c28yLmNvbSIsImh0dHBzOi8vd3NvMi5jb20_ZHVt" +
            "bXkxPTEmZHVtbXk9MiJdLCJ0b2tlbl9lbmRwb2ludF9hdXRoX3NpZ25pbmdfYWxnIjoiUFMyNTYiLCJ0b2tlbl9lbmRwb2ludF9h" +
            "dXRoX21ldGhvZCI6InByaXZhdGVfa2V5X2p3dCIsImdyYW50X3R5cGVzIjpbImNsaWVudF9jcmVkZW50aWFscyIsImF1dGhvcml6" +
            "YXRpb25fY29kZSIsInJlZnJlc2hfdG9rZW4iLCJ1cm46aWV0ZjpwYXJhbXM6b2F1dGg6Z3JhbnQtdHlwZTpqd3QtYmVhcmVyIl0s" +
            "InJlc3BvbnNlX3R5cGVzIjpbImNvZGUgaWRfdG9rZW4iXSwiYXBwbGljYXRpb25fdHlwZSI6IndlYiIsImlkX3Rva2VuX3NpZ25l" +
            "ZF9yZXNwb25zZV9hbGciOiJQUzI1NiIsImlkX3Rva2VuX2VuY3J5cHRlZF9yZXNwb25zZV9hbGciOiJSU0EtT0FFUCIsImlkX3Rv" +
            "a2VuX2VuY3J5cHRlZF9yZXNwb25zZV9lbmMiOiJBMjU2R0NNIiwicmVxdWVzdF9vYmplY3Rfc2lnbmluZ19hbGciOiJQUzI1NiIs" +
            "InNjb3BlIjoiYWNjb3VudHMiLCJzb2Z0d2FyZV9zdGF0ZW1lbnQiOiJleUpoYkdjaU9pSlFVekkxTmlJc0ltdHBaQ0k2SWtkeGFF" +
            "dFdWRUZPYmt4TldYQkhSMlpCZEVveFRtaGthMmRxZHlJc0luUjVjQ0k2SWtwWFZDSjkuZXlKcGMzTWlPaUpQY0dWdVFtRnVhMmx1" +
            "WnlCTWRHUWlMQ0pwWVhRaU9qRTJNekV3T0RNMU5UVXNJbXAwYVNJNklqZG1ZMlJsWTJFellXSmhNVFF4TnpJaUxDSnpiMlowZDJG" +
            "eVpWOWxiblpwY205dWJXVnVkQ0k2SW5OaGJtUmliM2dpTENKemIyWjBkMkZ5WlY5dGIyUmxJam9pVkdWemRDSXNJbk52Wm5SM1lY" +
            "SmxYMmxrSWpvaWRUTmFWMnhtT1ZsME5ESmtlVnBuU1haNmEzWnhZaUlzSW5OdlpuUjNZWEpsWDJOc2FXVnVkRjlwWkNJNkluVXpX" +
            "bGRzWmpsWmREUXlaSGxhWjBsMmVtdDJjV0lpTENKemIyWjBkMkZ5WlY5amJHbGxiblJmYm1GdFpTSTZJbGRUVHpJZ1QzQmxiaUJD" +
            "WVc1cmFXNW5JRlJRVURFZ0tGTmhibVJpYjNncElpd2ljMjltZEhkaGNtVmZZMnhwWlc1MFgyUmxjMk55YVhCMGFXOXVJam9pVjFO" +
            "UE1pQlBjR1Z1SUVKaGJtdHBibWNpTENKemIyWjBkMkZ5WlY5MlpYSnphVzl1SWpveExqVXNJbk52Wm5SM1lYSmxYMk5zYVdWdWRG" +
            "OTFjbWtpT2lKb2RIUndjem92TDNkemJ6SXVZMjl0SWl3aWMyOW1kSGRoY21WZmNtVmthWEpsWTNSZmRYSnBjeUk2V3lKb2RIUndj" +
            "em92TDNkemJ6SXVZMjl0SWl3aWFIUjBjSE02THk5M2MyOHlMbU52YlQ5a2RXMXRlVEU5TVNaa2RXMXRlVDB5SWwwc0luTnZablIz" +
            "WVhKbFgzSnZiR1Z6SWpwYklsQkpVMUFpTENKQlNWTlFJaXdpUTBKUVNVa2lYU3dpYjNKbllXNXBjMkYwYVc5dVgyTnZiWEJsZEdW" +
            "dWRGOWhkWFJvYjNKcGRIbGZZMnhoYVcxeklqcDdJbUYxZEdodmNtbDBlVjlwWkNJNklrOUNSMEpTSWl3aWNtVm5hWE4wY21GMGFX" +
            "OXVYMmxrSWpvaVZXNXJibTkzYmpBd01UVTRNREF3TURGSVVWRnlXa0ZCV0NJc0luTjBZWFIxY3lJNklrRmpkR2wyWlNJc0ltRjFk" +
            "R2h2Y21sellYUnBiMjV6SWpwYmV5SnRaVzFpWlhKZmMzUmhkR1VpT2lKSFFpSXNJbkp2YkdWeklqcGJJbEJKVTFBaUxDSkJTVk5R" +
            "SWl3aVEwSlFTVWtpWFgwc2V5SnRaVzFpWlhKZmMzUmhkR1VpT2lKSlJTSXNJbkp2YkdWeklqcGJJbEJKVTFBaUxDSkRRbEJKU1NJ" +
            "c0lrRkpVMUFpWFgwc2V5SnRaVzFpWlhKZmMzUmhkR1VpT2lKT1RDSXNJbkp2YkdWeklqcGJJbEJKVTFBaUxDSkJTVk5RSWl3aVEw" +
            "SlFTVWtpWFgxZGZTd2ljMjltZEhkaGNtVmZiRzluYjE5MWNta2lPaUpvZEhSd2N6b3ZMM2R6YnpJdVkyOXRMM2R6YnpJdWFuQm5J" +
            "aXdpYjNKblgzTjBZWFIxY3lJNklrRmpkR2wyWlNJc0ltOXlaMTlwWkNJNklqQXdNVFU0TURBd01ERklVVkZ5V2tGQldDSXNJbTl5" +
            "WjE5dVlXMWxJam9pVjFOUE1pQW9WVXNwSUV4SlRVbFVSVVFpTENKdmNtZGZZMjl1ZEdGamRITWlPbHQ3SW01aGJXVWlPaUpVWldO" +
            "b2JtbGpZV3dpTENKbGJXRnBiQ0k2SW5OaFkyaHBibWx6UUhkemJ6SXVZMjl0SWl3aWNHaHZibVVpT2lJck9UUTNOelF5TnpRek56" +
            "UWlMQ0owZVhCbElqb2lWR1ZqYUc1cFkyRnNJbjBzZXlKdVlXMWxJam9pUW5WemFXNWxjM01pTENKbGJXRnBiQ0k2SW5OaFkyaHBi" +
            "bWx6UUhkemJ6SXVZMjl0SWl3aWNHaHZibVVpT2lJck9UUTNOelF5TnpRek56UWlMQ0owZVhCbElqb2lRblZ6YVc1bGMzTWlmVjBz" +
            "SW05eVoxOXFkMnR6WDJWdVpIQnZhVzUwSWpvaWFIUjBjSE02THk5clpYbHpkRzl5WlM1dmNHVnVZbUZ1YTJsdVozUmxjM1F1YjNK" +
            "bkxuVnJMekF3TVRVNE1EQXdNREZJVVZGeVdrRkJXQzh3TURFMU9EQXdNREF4U0ZGUmNscEJRVmd1YW5kcmN5SXNJbTl5WjE5cWQy" +
            "dHpYM0psZG05clpXUmZaVzVrY0c5cGJuUWlPaUpvZEhSd2N6b3ZMMnRsZVhOMGIzSmxMbTl3Wlc1aVlXNXJhVzVuZEdWemRDNXZj" +
            "bWN1ZFdzdk1EQXhOVGd3TURBd01VaFJVWEphUVVGWUwzSmxkbTlyWldRdk1EQXhOVGd3TURBd01VaFJVWEphUVVGWUxtcDNhM01p" +
            "TENKemIyWjBkMkZ5WlY5cWQydHpYMlZ1WkhCdmFXNTBJam9pYUhSMGNITTZMeTlyWlhsemRHOXlaUzV2Y0dWdVltRnVhMmx1WjNS" +
            "bGMzUXViM0puTG5Wckx6QXdNVFU0TURBd01ERklVVkZ5V2tGQldDOTFNMXBYYkdZNVdYUTBNbVI1V21kSmRucHJkbkZpTG1wM2Ez" +
            "TWlMQ0p6YjJaMGQyRnlaVjlxZDJ0elgzSmxkbTlyWldSZlpXNWtjRzlwYm5RaU9pSm9kSFJ3Y3pvdkwydGxlWE4wYjNKbExtOXda" +
            "VzVpWVc1cmFXNW5kR1Z6ZEM1dmNtY3VkV3N2TURBeE5UZ3dNREF3TVVoUlVYSmFRVUZZTDNKbGRtOXJaV1F2ZFROYVYyeG1PVmww" +
            "TkRKa2VWcG5TWFo2YTNaeFlpNXFkMnR6SWl3aWMyOW1kSGRoY21WZmNHOXNhV041WDNWeWFTSTZJbWgwZEhCek9pOHZkM052TWk1" +
            "amIyMGlMQ0p6YjJaMGQyRnlaVjkwYjNOZmRYSnBJam9pYUhSMGNITTZMeTkzYzI4eUxtTnZiU0lzSW5OdlpuUjNZWEpsWDI5dVgy" +
            "SmxhR0ZzWmw5dlpsOXZjbWNpT2lKWFUwOHlJRTl3Wlc0Z1FtRnVhMmx1WnlKOS5uQjJXZUN0NFJKd3hjWlhTYTdDRGFjYkJibVNV" +
            "UDQzYWFhTUdObjRaRmxrYlh0UWI4dHQzNnpsVlVFRlpkZzc4ZTJVaWh4bU5BMTVNNEg4WGdlRDFOQzFlQTFIUWduQms3UEs4UjI3" +
            "YzZCRGdCSWVYdlU1ZHc4Q0FWVjF5TzYwSllWSVN0VXM4bFRsLVIxdV9YSDhKLTB2TS1DdF9wNm9XMlFwLWdPNmIyUjl6Mjltb0J3" +
            "bWphSEpkOHY0YzJpUE9IWWtzal8zbW8tb3BUdDBibEFJYkFMYlJKdDFzcDlMaTh1eld4OEJCMm9zMmxNdVR0TF9ULUFZZ2l1ekd6" +
            "OHJUSUtZV1EzMy1VUzVUV2t4ZDNlUm13QmJVTXhPelMwRzdhS1dHRFlidTlXcWJVWDY2bk45THVpdDZxYTY5Y0trbmhWVVZDeEkt" +
            "ZGtSU3FPVVRPakZMUHcifQ.XoA1SADTCLAby4twQ-rWNfcjC_IAxc6nDPdqjke2l-V4QPF9n9qQKilCuIlm-5JlNz8ks0wlzgJb1" +
            "NlOGIe9h0iEWlAX5T2ffPxQW7zPhSHLUwMYxPHjw6eRF4pajGHW5-sBzQWHwA9IkThliQAZ4FIzrYCNLJd4aKs-ac_RUYKFdMfia" +
            "j3JhG62egQV4iphbP1d4VsI7TsMjr8v2a9NJNwXu3X1LeRL-J9aQcE5lTB0mWAuEppvmTvj_gDl7pH3Hi_eCMsHOBiWqAEaWwgV1" +
            "iE1UaDjKn_zAg4TMSXCXYllen2YlmKrwyPabK16-YTiAOytMD_rlZusYpAvGnKPeg";

    @DataProvider(name = "jwtData")
    public static Object[][] jwtData() {

        return new Object[][] {
                {JWT_TOKEN, "head"},
                {JWT_TOKEN, "body"}
        };
    }
}
