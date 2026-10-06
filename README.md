# CloudStream Extensions

CloudStream eklentisi geliştirmek ve kendi eklenti deponu kurmak için başlangıç şablonu. Örnek modül: `ExampleProvider`.

## Depoyu CloudStream’e ekleme

CloudStream’de **Ayarlar → Eklentiler → Depo ekle** bölümüne şu adresi girin:

```text
https://raw.githubusercontent.com/Yek5353/cloudstream-extensions/main/repo.json
```

Depo listesi `builds` dalındaki `plugins.json` dosyasını okur. Henüz yayımlanmış eklenti yok.

## Eklenti geliştirme

1. `ExampleProvider` klasörünü kopyalayıp yeni eklenti için yeniden adlandırın.
2. `ExamplePlugin.kt` içinde eklentiyi kaydedin. `ExampleProvider.kt` içinde sağlayıcı adını, site adresini, dili ve arama davranışını düzenleyin.
3. Modülün `build.gradle.kts` dosyasında açıklama, yazar, tür, dil ve sürümü ayarlayın.
4. JDK 17, Android SDK ve Gradle kurulu bilgisayarda proje kökünde `gradle make makePluginsJson` komutunu çalıştırın.
5. GitHub **Actions → Build CloudStream plugins** akışı da her push’ta derler. `.cs3` paketleri ve `plugins.json` dosyası Actions çalıştırmasının **Artifacts** bölümünde bulunur.

Actions şu anda derleme çıktısını artifact olarak saklıyor; CloudStream içinden kurulabilir hale getirmek için `.cs3` paketlerini `builds` dalına koyup `plugins.json` listesini güncellemek gerekir.

## Kaynaklar

- [CloudStream eklenti şablonu kılavuzu](https://recloudstream.github.io/csdocs/devs/using-plugin-template/)
- [JSON repo manifesti kılavuzu](https://recloudstream.github.io/csdocs/devs/create-your-own-json-repository/)
- [Resmî TestPlugins şablonu](https://github.com/recloudstream/TestPlugins)
