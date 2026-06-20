package com.example.kpop.mockData

import com.example.kpop.R
import com.example.kpop.model.Product

object MockData {

    val productList = listOf(

        Product(
            name = "Aespa - Armageddon",
            category = "Album",
            price = 129,
            description = "Official aespa Armageddon Album.\nIncludes CD, Photobook, Random Photocard and Poster.",
            rating = 4.8,
            image = R.drawable.aespa
        ),

        Product(
            name = "IU - The Winning",
            category = "Album",
            price = 129,
            description = "IU The Winning Album.\nIncludes CD, Photobook and Random Photocard.",
            rating = 4.8,
            image = R.drawable.banner3
        ),

        Product(
            name = "IU Lightstick",
            category = "Lightstick",
            price = 269,
            description = "Official IU Lightstick perfect for concerts and fan meetings.",
            rating = 4.9,
            image = R.drawable.iu_lightstick
        ),

        Product(
            name = "BABYMONSTER Lightstick",
            category = "Lightstick",
            price = 269,
            description = "Official BABYMONSTER Lightstick.",
            rating = 4.9,
            image = R.drawable.bm_stick
        ),

        Product(
            name = "NMIXX Merch",
            category = "Merch",
            price = 159,
            description = "Official NMIXX merchandise with premium quality.",
            rating = 4.7,
            image = R.drawable.nmixx_merch
        ),

        Product(
            name = "IU Merch",
            category = "Merch",
            price = 159,
            description = "Official IU merchandise collection.",
            rating = 4.7,
            image = R.drawable.iu_merch
        ),

        Product(
            name = "LE SSERAFIM Photocard",
            category = "Photocard",
            price = 35,
            description = "Official LE SSERAFIM random photocard set.",
            rating = 4.6,
            image = R.drawable.le_photocard
        ),

        Product(
            name = "Aespa Photocard",
            category = "Photocard",
            price = 35,
            description = "Official aespa random photocard set.",
            rating = 4.6,
            image = R.drawable.aespa_pho
        )

    )
}