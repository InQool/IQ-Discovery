const KEY = 'token';

const TOKENS = {
    'dod.szabo': 'eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI4YzVlZDExZS00NTQ0LTRiYmEtOTEyZS1iOGRiNjE2YjdmNzYiLCJpc3MiOiJ0b2tlbiIsImlhdCI6MTQzNzY1ODA5OSwic3ViIjoiZG9kLnN6YWJvIiwiZXhwIjoxNTI0MDU4MDk5LCJuYmYiOjE0Mzc2NTgwOTksImdyb3VwIjoiNjQ4Iiwicm9sZSI6ImN1cmF0b3IifQ.UFWLx7rUe_vDjJ_tc7vuglGjbIJTMe_dyL8gx1Gbf-WmupX0t7hLVBPWOVVMmjSOZ3rHPjwNuS-JQhOGubpCdBJFacswGBli7lrnabWBJ6o-J1OxGgTFFxGLDm101hHqrS7QFF6QZDe_AH_toA8vM5ffKbcqorkfODaAVlc3_QStyBT7wDP7MF6Y8ulAowt7o75L5I_4Zy5QjYGd4mVkEtJcC5T0WobgFkNj2wTEougYXvYA4PSSJSVpL7c0yCkN8iee9KBQQAlT16aovF1NxmOA9taK6MXlgap7Lw_c8CNuG7zHf0QKqgqaCIkjwlR4SZCF3XdJviEDNd8_mcp4zg',
    jake: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI5NjNmOTY3Mi02OWU1LTQwZmMtOTVmZi1lY2I0NzJiZmFhMGIiLCJpc3MiOiJ0b2tlbiIsImlhdCI6MTQzNzY1ODA5OCwic3ViIjoiamFrZSIsImV4cCI6MTUyNDA1ODA5OCwibmJmIjoxNDM3NjU4MDk4LCJncm91cCI6IjY0OCIsInJvbGUiOiJyZWRhY3RvciJ9.j0prANjc-vpEnCKS0IPYVLRchAGmIWaNhvryjFzwZr-vJTzM_m5pgKRIixc8NmigRqNLbI9orG0rTguQhV7qbYiEiFghEp3ju82fo75quBNHeh8ba1j9ebjSqqs3xkCp1AXl-P4gc6sj2nX7BdJcZp478YnrXKUTCAKkDfJJo9FCPTlZwf8uHXsys5uu2LSfsBGzgUDvO2cMHIbmEyu6t7qfXJSAPc1GiZkjJBO_OjlEVoozF1NXcYwqKbvsq4zHVLC0zwpEvXgL3fKvUG-d6p1Qbn4Z1XEfb5JHBR8Q9HYo3q6VJKWTkRicjtimTLjEv4PTu6kIjqEpA3aHZIuwLw',
    'dod.zamborsky': 'eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiIwMDc0MmM2NC0yOTBhLTQ1NzItYmNlMi02ODFhNjhhMzhiNzEiLCJpc3MiOiJ0b2tlbiIsImlhdCI6MTQzNzY1ODA5OSwic3ViIjoiZG9kLnphbWJvcnNreSIsImV4cCI6MTUyNDA1ODA5OSwibmJmIjoxNDM3NjU4MDk5LCJncm91cCI6IjY0OCIsInJvbGUiOiJzeXNfYWRtaW4ifQ.rymdWeTb-0QJiOKPRWRDLKm-ERMtHx2Wu5rmEHWJQDeEpwBGPSTCty2XsNITuvzKiZE5IN06GYBC5pEN-qGzl8SGyoIT3dE7UNKdO28MEOJ4TCkpHtSdHQonANn0rQt5JqDZzQXEYhMQL-GH2G5ULpatYmAa17N3F9oDWGSbWbSLtNnQ3y7dTE1OZ5D9RiFsY-XqvZAP7Ociiol220I1QM1WznCfudDi4k3DnjMaq4u_t17QaP9LKU6Taoroqd5VmA_B8BrMZ2Caj1JiUj22WbmdtrNRrkG453ul9GSSqqKiNuRFB8sN6gvyFO3SBHsYE00RSgaojQqAgr1Tu9VGiA',
    'dod.hyza': 'eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJlNTMxOTRjNy0wY2ViLTRmMzYtOTJlZS03Y2Q5ZWIzYTNhNmEiLCJpc3MiOiJ0b2tlbiIsImlhdCI6MTQzNzY1ODA5OSwic3ViIjoiZG9kLmh5emEiLCJleHAiOjE1MjQwNTgwOTksIm5iZiI6MTQzNzY1ODA5OSwiZ3JvdXAiOiI2NDgiLCJyb2xlIjpbImN1cmF0b3IiLCJvcmdfYWRtaW4iXX0.H7IYFNLNBPxZetlll-2Tx0RwFAzwBnHrhBnX381jFIUAKAp5YUhxpsFG99ssIAWaWSZaP3UsGJLV00PavimRZhB7kr_6d06f7to3t9AjYYxMwBU-pY3BIvcbQnbKwlltDH0wmZ_ZF9U9uCtwhHnyPs-Mfw0JRjE1JZhH1PPjtld0gPbPxUPDAfns385rARxkA8vcIscGM24KOsR_faz8M-D9lD8fam5MTFqmLpSxZY785QqIY2xmDdptmZuOgMlUIlT_yWHoYCaTLBYuVdevPLjZ7rCGL4T1jSdbAW2GCYddUBEAPwZJtt3sCr2Q519x_41df_wxBBcO-IRYvIlgKw',
    'mach.v': 'eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI0NjgzNGU2NC1jOWY3LTRiZDctOTMxOC1hOTI2ZjVmM2ZkYTYiLCJpc3MiOiJ0b2tlbiIsImlhdCI6MTQzNzY1ODA5OCwic3ViIjoibWFjaC52IiwiZXhwIjoxNTI0MDU4MDk4LCJuYmYiOjE0Mzc2NTgwOTgsImdyb3VwIjoiMSIsInJvbGUiOlsiY3VyYXRvciIsInN5c19hZG1pbiIsInJlZGFjdG9yIiwib3JnX2FkbWluIl19.r9Du372pWgbL3mHd5QqCYMd8ymoEkk--AiSpRlk2k4JJWxY8i8b88eMDTpndvEp71o151qbKV5D_pEgoWuUx99ap5dO3BSbi5mTAwSFDco3rXHv8nwrPsKAbRb98CSudF0f6DfhwTbTm-17PnNPST7sMvWiOPKkP1acSUhlLoRVYgMc_vjPE2hkQb5xLo26CXdmVPf27L7y4PHYGpJPwt0oPt8tVKj1uzdo6jCuYcc38VxuMkfk8BiWE6Y7r5dx-RFMk8ekyxkN_u0_b6IVeU1qGhI0OpfpS-PbeJf6AI-VckxQetS6t7YSXnOeeekLPOrP_2mPnIPmYJDmDlb-B-A',
    'dod.skopal': 'eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI1YjIxYTM4Ni00NGI1LTQxMDQtOTY2Yi1mYjg0NDQ0NjdiZGEiLCJpc3MiOiJ0b2tlbiIsImlhdCI6MTQzNzY1ODA5OSwic3ViIjoiZG9kLnNrb3BhbCIsImV4cCI6MTUyNDA1ODA5OSwibmJmIjoxNDM3NjU4MDk5LCJncm91cCI6IjY0OCIsInJvbGUiOiJyZWRhY3RvciJ9.l2ACTi1d_gwf4bgJRPVfMIzeiYecDKMPIqwZJFr0-_xr76_dRUL1Qj1S2qoDjg66WK_8eOGEUndou1QO3o1ZZzEq0ytTTT-5lJh6Hnqx569qS7R45HKQAWhAKMMYZ4NQjN8cSiHTg70J_7S4ScpVmOn9tat4n6Ea5-b15H_ua_Kefhv5rGqCTbRg0kWYjwZWkzI84MDO5eJ9DnS7K7sjGY4codAqFGAHqqQ-90RFovPh0vL-bXJdtDiy0CDstmc0Yyg-DMPA-a5cBzqeGr5CkuddqBunLbz0Qz5LS-CX2s8Eqw7o1D9j81CR1F8odRo4EAq6MaC4J2U8ZpVdqNezSA',
    kudlajz: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI3OThmMGU0My00NWY4LTRjMzEtYTFhZC1jYTUzMGVhM2JlMzciLCJpc3MiOiJ0b2tlbiIsImlhdCI6MTQzNzY1ODA5OSwic3ViIjoia3VkbGFqeiIsImV4cCI6MTUyNDA1ODA5OSwibmJmIjoxNDM3NjU4MDk5LCJncm91cCI6IjY2NiIsInJvbGUiOiJyZWRhY3RvciJ9.Rsn2FLpBvWAwgyNwzRmypei6A0TnKeZNv_DCDYo82MJTNBsN_L1DHPeKU-Yqkyat6o0YJzjTFNxLjZpRgTCrMO6JEFKT_zWETXp3r2SO6UdTh-XvErp2R5U80asP9g5XdaazbzlHAfIkkJEcxfpWVBQSLRjWrSaiobe2ixnfG6s9MI7PhvQv4GlWHoWFUOtKI75LoUNKkaHJMBaAVCCERRbhNAw8L1c2w-hVYhdhjtgrMxEpYRK-4JSaqpcsRbdswfYuti5AytwGCns5p85q1r3lLiaZ9icbPQj_lKTHcsLZZYOwNElwrIjvc3QzL32V5qZg9lyImPl-DSZQiX8Rnw',
    'jane': 'eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiIzNzRlNzYzNC03NGY3LTRlZDMtODg3ZS0wNDE3OGE4NjA3MDYiLCJpc3MiOiJ0b2tlbiIsImlhdCI6MTQzNTkxMDYxOSwic3ViIjoiamFuZSIsImV4cCI6MTUyMjMxMDYxOSwibmJmIjoxNDM1OTEwNjE5LCJncm91cCI6IjY0OCIsInJvbGUiOiJjdXJhdG9yIn0.SB66YEsygPBrl4hIc71zBRqQREm0j3CjZHyf6Frepo6oi6ZkqLZEBzdK24t6GexYAxWo9Fa4UIQSqxilE73Hti9tYBLpfNE0NX-MOuQVfyPLHPANNBJGdxOxFGyZ04c-bfuPeIg9BEEqKKO54oQGxXlHdeRUn79FcOeSKlqLgDQchns-DT_C_S-1Dt81SyXX0GFvXHKmzDaARD32txsmCyURjttqGYwZVG1Ks6V3OMBn_mRdl3s5MJX5Kg090oCAkExaobKOM_sRPGKx_nL12ocyuVuE4bAU4nmcg2rxt-eRJgPXKJi0JFuYHmvF6fx6_8JZV3QlP5E_-RpaEgHK-A',
    kapurka: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI3MDJlNWU0Yy00ZDg2LTRkM2YtOGIxYi1jYzViMjZkMzY4YWMiLCJpc3MiOiJ0b2tlbiIsImlhdCI6MTQzNzY1ODA5OSwic3ViIjoia2FwdXJrYSIsImV4cCI6MTUyNDA1ODA5OSwibmJmIjoxNDM3NjU4MDk5LCJncm91cCI6IjY2NiIsInJvbGUiOiJjdXJhdG9yIn0.ccR3HkCTG9XZijovxLjSJAUnmXto-gCWyWScnQ7jgZ-9_UgJB68Ym6Zf9H5HO-qUqxAg0kj1H7mgkTRNIZyUQLxFjfL_t_t-TsbXLWNC4ZvlqNV9aHwXUD7GJSrXaNEsan1kmWhteOnJubD2wi291fWxAfZnaBlazuMcVK8ZhYuUfvABJTuooGWQ_9ei0bv3OUL2TQNLhPV1mz1YAcT_Jnk7bDyMUtlxwDbZLspG1CoXrdyAyikaOUzyJgSgseWYHGxIL-SeZSGMVZKJ8oqZXv-rvTIJ_7nC6ndC4F_MbhC9UHFwrJCUoEoC_Pm3qGHS91pCrDaW-1x5LDeuNdQ3dg',
    gady: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiIwYWJhMWIwZC1iMjlhLTRlMTgtYWFlZS02ZGM5YWFjMGU1ZjUiLCJpc3MiOiJ0b2tlbiIsImlhdCI6MTQzNzY1ODA5OSwic3ViIjoiZ2FkeSIsImV4cCI6MTUyNDA1ODA5OSwibmJmIjoxNDM3NjU4MDk5LCJncm91cCI6IjY2NiIsInJvbGUiOiJvcmdfYWRtaW4ifQ.mqpdpAotXLyLSPSiN_H6W-u6xjg-GEIjvxYSiUavdMrighew23QhxZaC7Ql-6vei2lXPjJauK2Lmih4j92-3SbA6zVPF_9rE0577nWj_eNqau8Dx9qjnFNt3X4s1SkWFLR_9CYqzETZAr_It97hPZX9ADAE6wMN-bXfgTv1rHpNJ1dj_kZITNkl3NNlqNDnzCxTmaOtyUyls44I3ty6r-pVwgW-DGjZeIpOT7wJqJl9jOvhwd8ZFXYbGZ1IjnhfOADldb034tsFd5K7j5E8dYvhCIzFiiO-r8nKx2h9F9fNA_-I95OZrVxKoC5xOrERgv-tN8oEjFKzg8XtccJhcag',
    majo: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJhNmQ5NGIyYy1lNDg3LTQwYmQtYmJkOC05NTEwNDViYWU5MmUiLCJpc3MiOiJ0b2tlbiIsImlhdCI6MTQzNzY1ODA5OSwic3ViIjoibWFqbyIsImV4cCI6MTUyNDA1ODA5OSwibmJmIjoxNDM3NjU4MDk5LCJncm91cCI6IjY0OCIsInJvbGUiOiJzeXNfYWRtaW4ifQ.qvxHyV2yyKItn8naoYkfJ72EOAAm5gxyP3znKfHMUUMEnJdI6bcxQHR9dC4OuY4QOylKXWcWtitU57eJa17rDN6ggYbXtR9O1YGyCzkfF351mxnFsmLz2ObRq2VwygobRGODHEL7a3oBnoBf4RpkEIjHGVeM8m9TSI4wByzMKJA-S73r0wwciMkdlPpvRgJYKN5S69I8G9vF-v0HMIXydlCy7fFuIHp7DbsUjzo2mecIFS63dd0-4hsq355XnEJi2BjFsR6HqKQcPvU4ZsD2oXIZVQX_kHwjW93SlPZDGMWQiijCyatWC4lDoWG4JvXu3j5OkaVesosix5ehDgbKug',
    '70947422.zdotest': 'eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiIzNGMzMWIxMy1kODI4LTRlZjMtYmQ4Ni03MWM0N2NkN2U3MGMiLCJpc3MiOiJ0b2tlbiIsImlhdCI6MTQzNzY1ODA5OSwic3ViIjoiNzA5NDc0MjIuemRvdGVzdCIsImV4cCI6MTUyNDA1ODA5OSwibmJmIjoxNDM3NjU4MDk5LCJncm91cCI6IjMxMiIsInJvbGUiOiJjdXJhdG9yIn0.EVWasetDyG4Y0WgIC5cYkQWagV0-zZukDMRaRcVy329UCf35D3wq88SfjU3cHD9-Sw2vDs_y6ZVBUYgSpgORoA7mAs-c5Jz7GnDNGNCHxLcr59x4SSmGJ-ZOig9FDf02HkNhpRkVQ9HikxnbUwpA5o30TfsnyZTwaF_zTLh1WQwbKKYXdMbk96gFJP5MASmNCnSZ-abY22xCQiW4Kj2HG7lOh_r02U9FnBHmWfQCJ7gklDFHSA21Hu8wcAS__4xwQjh7pVorjFAdGN2Zo6idWkE1mRXShZybyUgDl3Sit8MAjgYZYX4l_nTO_SZVZPQyhsvec0w-EZiA_SDMxb0a9Q',
    '00092126.zdotest': 'eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJkNjkwYjZhYS1jN2U1LTQzNTctYjA0ZS1lNDlmNWM4OTY3NjAiLCJpc3MiOiJ0b2tlbiIsImlhdCI6MTQzNzY1ODA5OCwic3ViIjoiMDAwOTIxMjYuemRvdGVzdCIsImV4cCI6MTUyNDA1ODA5OCwibmJmIjoxNDM3NjU4MDk4LCJncm91cCI6IjMxNiIsInJvbGUiOlsiY3VyYXRvciIsInN5c19hZG1pbiIsInJlZGFjdG9yIiwib3JnX2FkbWluIl19.KaNeEUp87yDxRjOTbNDDoaoN4XeCJStkNKXzgTJEmRBk-OfmZsTicnKZpxGAXH3LxBNT3m1a-jQImRslAMreILyOndEZ6V9kqOb-R6W4s_jlTmhPbvMeGD2iYZyxrBTW0GPB190Pe9M30OMSWPWuta4V-46K6MyUTbpLxaWUPDFTjobPsD-5SEScsqCrl_99BDrBw-7-eUu2HQ8MVDRdAoIyF8ngrNQvSNUX6J7tTO0rMExEuRWxmaWM1zLjVbEL4_IEItBYHis0s28k_ZRbZbw_8ofWy-48ppR9ReqOOFS_HKBYP3m429A46oOkOo2KCq5UxJqthiAQHi8YE2xsvw'
};

let _info = {};

export class User {
    /**
     * @ngInject
     * @param Restangular
     * @param {Configuration} Configuration
     */
    constructor(Restangular, Configuration) {
        this.rest = Restangular;
        this.config = Configuration;
    }

    isLoggedIn() {
        return Boolean(this.getToken());
    }

    setToken(token) {
        localStorage.setItem(KEY, token);
    }

    getToken() {
        return localStorage.getItem(KEY);
    }

    getInfo() {
        return _info;
    }

    setInfo(info) {
        _info = info;
    }

    isLoginViaCredentialsAllowed() {
        return this.config.loginViaCredentialsAllowed;
    }

    /**
     * @param {Object} credentials
     * @param {String} credentials.username
     * @param {String} credentials.password
     */
    login(credentials) {
        if (TOKENS.hasOwnProperty(credentials.username)) {
            let token = TOKENS[credentials.username];
            this.setToken(token);
            return getInfo.call(this);
        } else {
            if (this.isLoginViaCredentialsAllowed()) {
                return this.rest.withConfig(RestangularConfigurer => {
                    RestangularConfigurer.setFullResponse(true);
                }).one('token').post('login', credentials).then(response => {
                    let token = response.headers('authctoken');
                    if (token) {
                        localStorage.setItem('token', token);
                    }

                    return getInfo.call(this);
                });
            } else {
                throw 'Toto uživatelské jméno neexistuje.';
            }
        }

        function getInfo() {
            return this.rest.one('token').get().then(info => {
                this.setInfo(info);
            });
        }
    }

    logout() {
        localStorage.removeItem(KEY);
        _info = {};
    }

    getUsername() {
        return _info.idmUsername;
    }

    getGroup() {
        return _info.organization;
    }

    isAllowed(state) {
        if (state.hasOwnProperty('data') && state.data.hasOwnProperty('allowed')) {
            return _.intersection(state.data.allowed, this.getRoles()).length > 0;
        }

        return true;
    }

    hasRole(role) {
        return _.indexOf(_info.roles, role) !== -1;
    }

    /**
     * @return {Array}
     */
    getRoles() {
        return _info.roles;
    }
}
