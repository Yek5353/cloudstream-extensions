# CloudStream Extensions
CloudStream eklenti deposu başlangıç noktası.
## Depoyu CloudStream'e ekleme
Repo GitHub'a yüklendikten sonra CloudStream içindeki **Ayarlar → Eklentiler → Depo ekle** bölümüne şu adresi girin:
```text
https://raw.githubusercontent.com/Yek5353/cloudstream-extensions/master/repo.json
```
Henüz eklenti eklenmedi. Eklenti kaynakları eklendiğinde CloudStream Gradle eklenti şablonuyla derlenip `builds/plugins.json` listesine yayımlanmalıdır.
## Eklenti geliştirme
CloudStream'in resmî [TestPlugins şablonunu](https://github.com/recloudstream/TestPlugins) temel alın. Yeni bir eklenti modülü ve GitHub Actions yayın akışı eklendiğinde `builds/plugins.json` listesine eklentiler yayımlanabilir.
## Kaynaklar
- [CloudStream eklenti şablonunu kullanma](https://recloudstream.github.io/csdocs/devs/using-plugin-template/)
- [JSON repo manifesti oluşturma](https://recloudstream.github.io/csdocs/devs/create-your-own-json-repository/)
