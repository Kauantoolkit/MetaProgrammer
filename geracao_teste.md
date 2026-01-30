-- oq eu fiz no front-- 


aba atributos: id, tipo conversão, source format, target format e status

aba relações: nenhuma

behaviors: 
    soft delete: on
    audit log: on
    versionamento: on
    Timestamps: on 
    state machine: off


api config: habilitados apenas autenticação, filtro por data e exportar csv.





-- oq veio no json:
    {
  "appName": "conversor",
  "entities": [
    {
      "name": "registro_de_conversão",
      "attributes": [
        {
          "name": "id",
          "type": "number",
          "constraints": [
            "primary_key",
            "auto_increment"
          ]
        },
        {
          "name": "tipo_da_conversão",
          "type": "enum",
          "constraints": [
            "required"
          ],
          "values": [
            "pdf-docx",
            "pdf-jpg",
            "pdf-png",
            "pdf-webp",
            "docx-pdf",
            "docx-jpg",
            "docx-png",
            "docx-webp",
            "jpg-png",
            "jpg-webp",
            "png-jpg",
            "png-webp",
            "webp-jpg",
            "webp-png",
            "mp4-mp3",
            "pdf-zip",
            "docx-zip",
            "jpg-zip",
            "png-zip",
            "webp-zip",
            "mp4-zip",
            "mp3-zip"
          ]
        },
        {
          "name": "source_format",
          "type": "string"
        },
        {
          "name": "target_format",
          "type": "string"
        },
        {
          "name": "status",
          "type": "enum",
          "values": [
            "Sucesso",
            "Erro",
            "Processando"
          ]
        }
      ],
      "relations": [],
      "behaviors": [
        "soft_delete",
        "audit_log",
        "versioning",
        "timestamps"
      ],
      "api": {
        "endpoints": [
          "filter_by_date_range",
          "export_csv"
        ],
        "auth": "required",
        "roles": [
          "admin"
        ]
      }
    }
  ],
  "interfaces": []
}


oq foi gerado: n o diretório conversor, em generated
[

    


]
